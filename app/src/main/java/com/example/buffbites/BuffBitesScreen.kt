package com.example.buffbites

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.buffbites.ui.OrderSummaryScreen
import com.example.buffbites.ui.OrderViewModel
import com.example.buffbites.ui.StartOrderScreen
import kotlinx.coroutines.flow.internal.NoOpContinuation.context
import javax.sql.DataSource
import kotlin.coroutines.jvm.internal.CompletedContinuation.context

enum class BuffBitesScreen(@StringRes val title: Int) {
    Start(title = R. string.app_name),
    Restaurant(title = R.string.choose_meal),
    Delivery(title = R. string.choose_delivery_time),
    Summary(title = R.string.order_summary)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuffBitesAppBar(
    currentScreen: BuffBitesScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier

){
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuffBitesApp(
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = BuffBitesScreen.valueOf(
        backStackEntry?.destination?.route ?: BuffBitesScreen.Start.name
    )

    Scaffold(
        topBar = {
            BuffBitesAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = {navController.navigateUp()}
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = BuffBitesScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = BuffBitesScreen.Start.name) {
                val context = LocalContext.current
                StartOrderScreen(
                    quantityOptions = DataSource.quantityOptions,
                    onNextButtonsClicked = {
                        viewModel.setQuantity(it)
                        navController.navigate(BuffBitesScreen.Restaurant.name)
                    }
                            modifier = Modifier
                            .fillMaxSize()
                        .padding(dimensionResource(id = R.dimen.padding_meduim))
                )
            }
            composable(route = BuffBitesScreen.Restaurant.name) {
                SelectOptionsScreen(
                    subtotal = uiState.price, // Define appropriate data source for uiState
                    onNextButtonClicked = {navController.navigate(BuffBitesScreen.Restaurant.name)},
                    onCancelButtonClicked = {
                                            cancelOrderAndNaviagteToStart(viewModel, navController)
                    },
                    options = DataSource.Restaurant.map { id -> context.resources.getString(id) },
                    onSelectionChanged = { viewModel.setRestaurant(it) },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = BuffBitesScreen.Delivery.name) {
                SelectOptionsScreen(
                    subtotal = uiState.price, // Define appropriate data source for uiState
                    onNextButtonClicked = {navController.navigate(BuffBitesScreen.Delivery.name)},
                    options = uiState.DeliveryOptions, // Define this data source
                    onSelectionChanged = { viewModel.setDate(it) },
                    onCancelButtonClicked = {},
                    modifier =Modifier.fillMaxHeight()
                )
            }
            composable(route = BuffBitesScreen.Summary.name) {
                val context = LocalContext.current
                OrderSummaryScreen(
                    orderUiState = uiState, // Define appropriate data source for uiState
                    onCancelButtonClicked = {
                                            cancelOrderAndNaviagteToStart(viewModel, navController)
                    },
                    onSendButtonClicked = {subject: String, summary: String ->}
                    shareOrder(context, subject = subject, summary = summary)
            }

        }
    }
}

private fun  cancelOrderAndNaviagteToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
){
    viewModel.resetOrder()
    navController.popBackStack()(BuffBitesScreen.Start.name, inclusive = false)
}

private fun shareOrder(context: Context, subject: String, summary, String){
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plan"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT,summary)
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.new_buffbites_order)
        )
    )
}


@Composable
fun SelectOptionsScreen(
    subtotal: Any,
    options: List<String>,
    onSelectionChanged: (String) -> Unit,
    onNextButtonClicked: () -> Unit,
    onCancelButtonClicked: () -> Unit
) {
    // Implement the logic for the SelectOptionsScreen here
}
