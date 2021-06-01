package com.example.androiddevchallenge.model

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.*

class RecipesListViewModel : ViewModel() {

    private val _list = MutableLiveData(RecipesDataGenerator.generateRecipes(2).toList())
    val list: LiveData<List<Recipe>> = _list

    private val _color = MutableLiveData(Color.Unspecified)
    val color: LiveData<Color> = _color

    private val _price = MutableLiveData<Double>(getRecipesPrice(list.value))
    val price: LiveData<Double> = _price

    fun initListener(lifecycleOwner: LifecycleOwner) {
        _list.observe(lifecycleOwner) {
            _price.value = getRecipesPrice(list.value)
        }
        _color.observe(lifecycleOwner) {
            _price.value = getRecipesPrice(list.value)
        }
    }

    fun setColor(color: Color) {
        _color.value = color
    }

    fun addRecipe() {
        val color = getNewRecipeColor()
        val newRecipe = RecipesDataGenerator.generateRecipe(
            color = color
        )
        val newList = _list.value?.toMutableList()
        newList?.add(newRecipe)
        _list.value = newList
    }

    fun deleteRecipe(id: Int) {
        val newList = _list.value?.toMutableList()
        newList?.removeAll { it.id == id }
        _list.value = newList
    }

    fun filterColors(recipe: Recipe, selectedColor: Color) =
        if (selectedColor == Color.Unspecified) {
            true
        } else {
            recipe.color == selectedColor
        }

    private fun getRecipesPrice(list: List<Recipe>?): Double {
        var sum = 0.0
        val newColor = _color.value ?: Color.Unspecified
        list
            ?.filter { filterColors(it, newColor) }
            ?.forEach { sum += it.price }

        return sum
    }

    private fun getNewRecipeColor() =
        if(_color.value == Color.Unspecified)
            RecipesDataGenerator.randomColor
        else
            _color.value ?: RecipesDataGenerator.randomColor
}
