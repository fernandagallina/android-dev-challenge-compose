package com.example.androiddevchallenge.model

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.*

class RecipesListViewModel : ViewModel() {

    private val _list = MutableLiveData(RecipesDataGenerator.generateRecipes(2).toList())
    val list: LiveData<List<Recipe>> = _list

    private val _price = MutableLiveData<Double>(getRecipesPrice(list.value))
    val price: LiveData<Double> = _price

    private val _color = MutableLiveData(Color.Unspecified)
    val color: LiveData<Color> = _color

    fun initListener(lifecycleOwner: LifecycleOwner){
        _list.observe(lifecycleOwner){
            _price.value = getRecipesPrice(list.value)
        }
    }

    fun setColor(color: Color) {
        _color.value = color
    }

    fun addRecipe(){
        val newRecipe = RecipesDataGenerator.generateRecipes(1).first()
        val newList = _list.value?.toMutableList()
        newList?.add(newRecipe)
        _list.value = newList
    }

    fun deleteRecipe(id: Int){
        val newList = _list.value?.toMutableList()
        newList?.removeAll { it.id == id }
        _list.value = newList
    }

    private fun getRecipesPrice(list: List<Recipe>? ): Double {
        var sum = 0.0
        list?.forEach {
            sum += it.price
        }
        return sum
    }
}
