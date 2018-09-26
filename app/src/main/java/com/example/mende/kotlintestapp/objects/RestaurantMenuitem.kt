package com.example.mende.kotlintestapp.objects

import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Marunizer
 *
 * Purpose of this class is to contain menu item information (Maybe model information too?) gathered from Database
 *
 * -------------------------------------------------------------------------------------------------
 * NOTES:
 *  Might use Atomic Integer in the future to find out when all necessary files are downloaded
 *  Possible we do not have to worry about this at all, depending on how downloads are managed and tracked
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 *  - using val within a real time database
 *  - How will the downloads be tracked
 *
 * -------------------------------------------------------------------------------------------------
 * TODO: Remove objPath eventually, draco path will provide draco file that converts obj
 */

data class RestaurantMenuitem(
        val name: String,
        val cost: String,
        val description: String,
        val drcPath: String,
        val objPath: String,
        val mtlPath: String,
        val jpgPath: String,
        val iconPath: String,
        val listOfCategory : ArrayList<String> //To hold the categories one item can be a part of
)
{
    var isDownloaded: Boolean = false //This is declared and initialized
    var isDrcDownloaded: Boolean = false
    var isObjDownlaoded: Boolean = false
    var isMtlDownloaded: Boolean = false
    var isJpgdownloaded: Boolean = false
}