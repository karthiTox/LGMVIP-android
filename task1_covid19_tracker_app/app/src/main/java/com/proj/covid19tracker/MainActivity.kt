package com.proj.covid19tracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.proj.covid19tracker.ui.theme.Covid19TrackerTheme
import android.net.NetworkInfo

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import java.lang.Exception


class MainActivity : ComponentActivity() {

    companion object {
        /**
         * COVID_DATA_URL's subdomain may change in future.
         */
        private const val COVID_DATA_URL = "https://data.covid19india.org/state_district_wise.json"
    }

    private lateinit var reqQueue: RequestQueue

    private val currentScreen = mutableStateOf(Screens.StatesListPage)

    private val stateName = mutableStateOf("")

    private val stateCode = mutableStateOf("")
    private val districtName = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reqQueue = Volley.newRequestQueue(this)

        setContent {
            Covid19TrackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (currentScreen.value) {
                                        Screens.StatesListPage -> "LGM"
                                        Screens.DistrictsListPage -> "${stateName.value} - ${stateCode.value}"
                                        Screens.DistrictDetailsPage -> districtName.value
                                    },
                                    color = MaterialTheme.colors.onPrimary
                                )
                            },
                            backgroundColor = MaterialTheme.colors.primary,
                            elevation = 0.dp,
                        )
                    }
                ) {
                    Box(Modifier.padding(horizontal = 20.dp)) {
                        when (currentScreen.value) {
                            Screens.StatesListPage -> StatesListPage()
                            Screens.DistrictsListPage -> DistrictsListPage()
                            Screens.DistrictDetailsPage -> ListDetailsPage()
                        }
                    }
                }
            }
        }
    }

    /**
     * Every states are displayed in Card Composable
     */
    @Composable
    private fun StatesListPage() {
        val statesList = remember { mutableStateListOf<String>() }

        fetchCovidStatesData {
            statesList.addAll(it.asSequence().toList())
            it.forEach {
                Log.i("testing", it)
            }
        }

        LazyColumn {
            if (statesList.size > 0)
                item {
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        "States",
                        style = MaterialTheme.typography.h6,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

            items(items = statesList) {
                Spacer(modifier = Modifier.height(10.dp))
                CusCard(text = it) {
                    stateName.value = it
                    currentScreen.value = Screens.DistrictsListPage
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    /**
     * Every Districts are displayed in Card Composable
     */
    @Composable
    fun DistrictsListPage() {
        val districtsList = remember { mutableStateListOf<String>() }

        fetchCovidDistrictData(stateName.value) { d, sc ->
            districtsList.addAll(d.asSequence().toList())
            stateCode.value = sc
        }

        LazyColumn {
            if (districtsList.size > 0)
                item {
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        "Districts",
                        style = MaterialTheme.typography.h6
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

            items(items = districtsList) {
                Spacer(modifier = Modifier.height(10.dp))
                CusCard(text = it) {
                    districtName.value = it
                    currentScreen.value = Screens.DistrictDetailsPage
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    @Composable
    private fun LazyItemScope.CusCard(text: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
            shape = RoundedCornerShape(5.dp),
            elevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    modifier = Modifier.weight(2.5F)
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    modifier = Modifier.weight(0.5F),
                    contentDescription = "next"
                )
            }
        }
    }

    @Composable
    private fun ListDetailsPage() {
        val notes = remember { mutableStateOf("") }
        val active = remember { mutableStateOf(0L) }
        val confirmed = remember { mutableStateOf(0L) }
        val migratedOther = remember { mutableStateOf(0L) }
        val deceased = remember { mutableStateOf(0L) }
        val recovered = remember { mutableStateOf(0L) }

        fetchCovidDistrictDetailsData(
            stateName.value,
            districtName.value
        ) { n, a, c, mo, d, r ->
            notes.value = n
            active.value = a
            confirmed.value = c
            migratedOther.value = mo
            deceased.value = d
            recovered.value = r
        }

        LazyColumn(
            Modifier.padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            if (notes.value != "")
                item {
                    Text(
                        style = MaterialTheme.typography.h5,
                        text = notes.value
                    )
                }
            item {
                Text(
                    "Active: ${active.value}",
                    style = MaterialTheme.typography.h6
                )
            }
            item {
                Text(
                    "Confirmed: ${confirmed.value}",
                    style = MaterialTheme.typography.h6
                )
            }
            item {
                Text(
                    "MigratedOther: ${migratedOther.value}",
                    style = MaterialTheme.typography.h6
                )
            }
            item {
                Text(
                    "Deceased: ${deceased.value}",
                    style = MaterialTheme.typography.h6
                )
            }
            item {
                Text(
                    "Recovered: ${recovered.value}",
                    style = MaterialTheme.typography.h6
                )
            }
        }
    }


    private fun fetchCovidStatesData(result: (states: Iterator<String>) -> Unit) {
        val statesReq = JsonObjectRequest(Request.Method.GET, COVID_DATA_URL, null, { response ->
            result(response.keys())
        }, { error ->
            Log.i("testing", error.message.toString())
        })

        reqQueue.add(statesReq)
    }

    private fun fetchCovidDistrictData(
        stateName: String,
        result: (districts: Iterator<String>, stateCode: String) -> Unit
    ) {
        val districtReq = JsonObjectRequest(Request.Method.GET, COVID_DATA_URL, null, { response ->
            val state = response.getJSONObject(stateName)
            result(
                state.getJSONObject("districtData").keys(),
                state.getString("statecode")
            )
        }, { error ->
            Log.i("testing", error.message.toString())
        })

        reqQueue.add(districtReq)
    }

    private fun fetchCovidDistrictDetailsData(
        stateName: String,
        districtName: String,
        result: (
            notes: String,
            active: Long,
            confirmed: Long,
            migratedOther: Long,
            deceased: Long,
            recovered: Long,
        ) -> Unit
    ) {
        val districtReq = JsonObjectRequest(Request.Method.GET, COVID_DATA_URL, null, { response ->

            val state = response.getJSONObject(stateName)
            val district = state.getJSONObject("districtData").getJSONObject(districtName)

            val notes = district.getString("notes")
            val active = district.getLong("active")
            val confirmed = district.getLong("confirmed")
            val migratedOther = district.getLong("migratedother")
            val deceased = district.getLong("deceased")
            val recovered = district.getLong("recovered")

            result(notes, active, confirmed, migratedOther, deceased, recovered)

        }, { error ->
            Log.i("testing", error.message.toString())
        })

        reqQueue.add(districtReq)
    }

    override fun onBackPressed() {
        when (currentScreen.value) {
            Screens.StatesListPage -> super.onBackPressed()
            Screens.DistrictsListPage -> currentScreen.value = Screens.StatesListPage
            Screens.DistrictDetailsPage -> currentScreen.value = Screens.DistrictsListPage
        }
    }
}
