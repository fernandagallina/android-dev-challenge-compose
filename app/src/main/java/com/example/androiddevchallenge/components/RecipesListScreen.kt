package com.example.androiddevchallenge.components

import android.util.Log
import androidx.annotation.ColorRes
import androidx.compose.animation.*
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.model.Recipe
import com.example.androiddevchallenge.model.RecipesDataGenerator
import com.example.androiddevchallenge.model.RecipesListViewModel
import com.example.androiddevchallenge.ui.theme.DarkGray
import com.example.androiddevchallenge.ui.theme.MyTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.dynamicanimation.animation.FlingAnimation
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Main task screen composable
 */
@ExperimentalAnimationApi
@Composable
fun RecipesListScreen(viewModel: RecipesListViewModel) {
    Column {
        val recipesList: List<Recipe> by viewModel.list.observeAsState(emptyList())
        val recipesPrice by viewModel.price.observeAsState(0.0)
        val color by viewModel.color.observeAsState(Color.Unspecified)
        val filteredRecipe = recipesList.filter { viewModel.filterColors(it, color) }
        val isEmptyView = filteredRecipe.isEmpty()
        ColorFilter(onColorClick = {
            viewModel.setColor(it)
        })
        if (isEmptyView) {
            EmptyView(Modifier.weight(1f))
        } else {
            RecipeListView(
                recipesList = filteredRecipe,
                modifier = Modifier.weight(1f),
                onDelete = {
                    viewModel.deleteRecipe(it)
                }
            )
        }
        BottomView(recipesPrice, onAddClick = {
            viewModel.addRecipe()
        })
    }
}

/**
 * Displays list of recipes
 */
@Composable
fun RecipeListView(
    recipesList: List<Recipe>,
    onDelete: (id: Int) -> Unit = {},
    modifier: Modifier
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier.background(DarkGray)
    ) {
        items(
            count = recipesList.size,
            key = {
                recipesList[it].id
            }
        ) { item ->
            val recipe = recipesList[item]

            var isInEditableMode by rememberSaveable(
                key = recipe.id.toString()
            ) { mutableStateOf(false) }

            if (isInEditableMode) {
                ConfirmDeletionCard(
                    recipe = recipe,
                    onConfirmCLick = {
                        onDelete.invoke(recipe.id)
                        isInEditableMode = false
                    },
                    onCancelClick = {
                        isInEditableMode = false
                    }
                )
            } else {
                RecipeCard(recipe,
                    onLongClick = {
                        isInEditableMode = true
                    },
                    onSwiped = {
                        onDelete.invoke(recipe.id)
                    }
                )
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
        }
    }
}

/**
 * Static box with Price + Button
 */
@ExperimentalAnimationApi
@Composable
fun BottomView(price: Double = 0.0, onAddClick: () -> Unit = {}) {
    Column {
        AnimatedVisibility(
            visible = (price != 0.0),
            enter = slideInVertically(
                initialOffsetY = { -40 },
            ) + expandVertically(
                expandFrom = Alignment.Top
            ) + fadeIn(initialAlpha = 0.3f),
            exit = slideOutVertically(targetOffsetY = { 40 })
                    + shrinkVertically(shrinkTowards = Alignment.Bottom)
                    + fadeOut(targetAlpha = 0.3f)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Total price",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = String.format("$ %.2f", price / 100),
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
        AddButton(onAddClick)
    }
}

/**
 * Draws an "Add" button
 */
@Composable
fun AddButton(onClick: () -> Unit) {
    Button(
        onClick = { onClick.invoke() },
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
    ) {
        Text(text = "Add recipe")
    }
}

/**
 * Card which displays a recipe with name, color and price
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeCard(recipe: Recipe, onLongClick: () -> Unit = {}, onSwiped: () -> Unit = {}) {
    var offsetX by remember { mutableStateOf(0f) }

    Card(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = {
                    onLongClick.invoke()
                },
                onClick = {}
            )
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .draggable(
                state = rememberDraggableState {
                    offsetX += it
                },
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    if (abs(offsetX) < 500) {
                        offsetX = 0f
                    } else {
                        onSwiped.invoke()
                    }
                }
            )
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            val centerVerticalAlignment = Modifier.align(Alignment.CenterVertically)
            ColorView(color = recipe.color, centerVerticalAlignment)
            RecipeName(
                recipe,
                centerVerticalAlignment
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            VerticalDivider(centerVerticalAlignment)
            RecipePrice(recipe, centerVerticalAlignment)
        }
    }
}

/**
 * Card which shows a request to remove a particular recipe from the list
 */
@Composable
fun ConfirmDeletionCard(
    recipe: Recipe,
    onConfirmCLick: () -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
    Card(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
    ) {

        Column(
            Modifier
                .background(color = recipe.color)
                .padding(16.dp),
        ) {
            Text(
                text = "Remove from the list?",
                modifier = Modifier.padding(8.dp)
            )
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val weightModifier = Modifier.weight(1f)
                ConfirmationButton(text = "Yes", modifier = weightModifier) {
                    onConfirmCLick.invoke()
                }
                ConfirmationButton(text = "No", modifier = weightModifier) {
                    onCancelClick.invoke()
                }
            }
        }
    }
}

/**
 * Small color indicator for a recipe
 */
@Composable
fun ColorView(color: Color, modifier: Modifier) {
    Spacer(
        modifier = modifier
            .width(8.dp)
            .height(52.dp)
            .background(color, shape = RoundedCornerShape(6.dp))
    )
}

/**
 * Displays a list of color tags available for filtering. Check [BonusComponentsReview]
 *
 * Use this view for Bonus task
 */
@Composable
fun ColorFilter(onColorClick: (Color) -> Unit = {}) {
    var selectedColor by remember { mutableStateOf(Color.Unspecified) }
    Row(
        Modifier
            .background(DarkGray)
            .padding(vertical = 8.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        RecipesDataGenerator.colors.forEach { color ->
            ColorView3(
                selectedColor = selectedColor,
                color = color,
                onColorClick = {
                    onColorClick.invoke(it)
                    selectedColor = it
                }
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ColorView3(
    selectedColor: Color,
    color: Color,
    modifier: Modifier = Modifier,
    onColorClick: (Color) -> Unit
) {
    Spacer(
        modifier = modifier
            .width(64.dp)
            .height(24.dp)
            .background(
                color = getColor(selectedColor, color),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                onColorClick.invoke(
                    if (selectedColor == color) {
                        Color.Unspecified
                    } else {
                        color
                    }
                )
            }
    )
}

fun getColor(selectedColor: Color, baseColor: Color) =
    baseColor.copy(alpha = if (baseColor == selectedColor) 1F else 0.5F)

@Preview
@Composable
fun BonusComponentsReview() {
    MyTheme {
        ColorFilter()
    }
}

@Preview
@Composable
fun ComponentsPreview() {
    MyTheme {
        Surface {
            Column {
                val recipe = RecipesDataGenerator.generateRecipes(1).first()
                RecipeCard(recipe)
                Spacer(modifier = Modifier.size(8.dp))
                ConfirmDeletionCard(recipe)
            }
        }
    }
}

//@Preview
//@Composable
//fun ScreenPreview() {
//    MyTheme {
//        RecipesListScreen()
//    }
//}