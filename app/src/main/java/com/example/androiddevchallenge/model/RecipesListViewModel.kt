package com.example.androiddevchallenge.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecipesListViewModel : ViewModel() {

    private val _list = MutableLiveData(RecipesDataGenerator.generateRecipes(10).toList())
    val list: LiveData<List<Recipe>> = _list

    // getting data to display on the UI from the data source
    // val recipesList = RecipesDataGenerator.generateRecipes(10)
}
