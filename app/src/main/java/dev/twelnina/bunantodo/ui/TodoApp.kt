package dev.twelnina.bunantodo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.twelnina.bunantodo.R
import dev.twelnina.bunantodo.data.local.TodoEntity
import dev.twelnina.bunantodo.model.PlannedDateFilter
import dev.twelnina.bunantodo.ui.calendar.CalendarScreen
import dev.twelnina.bunantodo.ui.edit.EditScreen
import dev.twelnina.bunantodo.ui.entry.AddScreen
import dev.twelnina.bunantodo.ui.home.HomeScreen
import dev.twelnina.bunantodo.ui.list.ListScreen
import dev.twelnina.bunantodo.ui.navigation.AppNavKey
import dev.twelnina.bunantodo.ui.theme.TodoAppTheme
import kotlinx.coroutines.launch

@Composable
fun TodoApp(
    viewModel: TodoAppViewModel = viewModel(factory = TodoAppViewModel.Factory)
) {
    val backStack = rememberNavBackStack(AppNavKey.TodoHome)
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    val todoDeletedMessage = stringResource(R.string.todo_snackbar_deleted)
    val undoLabel = stringResource(R.string.todo_snackbar_action_undo)
    val onTodoDeleted: (TodoEntity) -> Unit = { deletedTodo ->
        coroutineScope.launch {
            val result =
                snackbarHostState.showSnackbar(
                    message = todoDeletedMessage,
                    actionLabel = undoLabel,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long
                )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreDeletedTodo(deletedTodo)
            }
        }
    }

    val currentNavKey = backStack.lastOrNull()

    val showToolbar = when (currentNavKey) {
        AppNavKey.TodoHome,
        is AppNavKey.TodoList,
        AppNavKey.TodoCalendar -> true

        else -> false
    }

    TodoAppTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = showToolbar,
                    enter = slideInVertically(
                        animationSpec = tween(durationMillis = 200),
                        initialOffsetY = { fullHeight -> fullHeight }
                    ),
                    exit = slideOutVertically(
                        animationSpec = tween(durationMillis = 200),
                        targetOffsetY = { fullHeight -> fullHeight }
                    )
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        modifier = Modifier.height(56.dp),
                        floatingActionButton = {
                            FloatingToolbarDefaults.VibrantFloatingActionButton(
                                shape = CircleShape,
                                onClick = { backStack.add(AppNavKey.AddTodo) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.add_24px),
                                    contentDescription = stringResource(R.string.navigation_action_add_todo)
                                )
                            }
                        }
                    ) {
                        FloatingToolbarItem(
                            selected = currentNavKey == AppNavKey.TodoHome,
                            iconResourceId = R.drawable.home_24px,
                            stringResourceId = R.string.navigation_destination_home,
                            onClick = { backStack.navigateToTopLevel(AppNavKey.TodoHome) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FloatingToolbarItem(
                            selected = currentNavKey is AppNavKey.TodoList,
                            iconResourceId = R.drawable.list_24px,
                            stringResourceId = R.string.navigation_destination_list,
                            onClick = {
                                if (currentNavKey !is AppNavKey.TodoList) {
                                    backStack.navigateToTopLevel(AppNavKey.TodoList())
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FloatingToolbarItem(
                            selected = currentNavKey == AppNavKey.TodoCalendar,
                            iconResourceId = R.drawable.calendar_month_24px,
                            stringResourceId = R.string.navigation_destination_calendar,
                            onClick = { backStack.navigateToTopLevel(AppNavKey.TodoCalendar) }
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            val navDisplayPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection),
                top = innerPadding.calculateTopPadding(),
                end = innerPadding.calculateEndPadding(layoutDirection)
            )

            NavDisplay(
                backStack = backStack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(navDisplayPadding)
                    .consumeWindowInsets(navDisplayPadding),
                onBack = { backStack.removeLastOrNull() },
                transitionSpec = { topLevelTransition() },
                popTransitionSpec = { topLevelTransition() },
                predictivePopTransitionSpec = { topLevelTransition() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<AppNavKey.TodoHome> {
                        HomeScreen(
                            onEditTodo = { id ->
                                snackbarHostState.currentSnackbarData?.dismiss()
                                backStack.add(AppNavKey.EditTodo(id))
                            },
                            onDeleted = onTodoDeleted,
                            onSeeAllPastIncomplete = {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                backStack.navigateToTopLevel(
                                    AppNavKey.TodoList(
                                        initialPlannedDateFilter = PlannedDateFilter.PAST_INCOMPLETE
                                    )
                                )
                            }
                        )
                    }
                    entry<AppNavKey.TodoList> { key ->
                        ListScreen(
                            onEdit = { id ->
                                snackbarHostState.currentSnackbarData?.dismiss()
                                backStack.add(AppNavKey.EditTodo(id))
                            },
                            onDeleted = onTodoDeleted,
                            initialPlannedDateFilter = key.initialPlannedDateFilter
                        )
                    }
                    entry<AppNavKey.TodoCalendar> {
                        CalendarScreen(
                            onEdit = { id ->
                                snackbarHostState.currentSnackbarData?.dismiss()
                                backStack.add(AppNavKey.EditTodo(id))
                            },
                            onDeleted = onTodoDeleted
                        )
                    }
                    entry<AppNavKey.AddTodo>(
                        metadata =
                            NavDisplay.transitionSpec { todoFormEnterTransition() } +
                                    NavDisplay.popTransitionSpec { todoFormExitTransition() } +
                                    NavDisplay.predictivePopTransitionSpec {
                                        todoFormExitTransition()
                                    }
                    ) {
                        val snackbarMessage = stringResource(R.string.todo_snackbar_added)
                        AddScreen(
                            onBack = { backStack.removeLastOrNull() },
                            onSaved = {
                                backStack.removeLastOrNull()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(snackbarMessage)
                                }
                            }
                        )
                    }
                    entry<AppNavKey.EditTodo>(
                        metadata =
                            NavDisplay.transitionSpec { todoFormEnterTransition() } +
                                    NavDisplay.popTransitionSpec { todoFormExitTransition() } +
                                    NavDisplay.predictivePopTransitionSpec {
                                        todoFormExitTransition()
                                    }
                    ) { key ->
                        val todoUpdatedMessage = stringResource(R.string.todo_snackbar_updated)
                        val todoDeletedMessage = stringResource(R.string.todo_snackbar_deleted)
                        val undoLabel = stringResource(R.string.todo_snackbar_action_undo)
                        EditScreen(
                            id = key.id,
                            onBack = { backStack.removeLastOrNull() },
                            onUpdated = {
                                backStack.removeLastOrNull()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(todoUpdatedMessage)
                                }
                            },
                            onDeleted = { deletedTodo ->
                                backStack.removeLastOrNull()
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = todoDeletedMessage,
                                        actionLabel = undoLabel,
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Long
                                    )

                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreDeletedTodo(deletedTodo)
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun FloatingToolbarItem(
    selected: Boolean,
    iconResourceId: Int,
    stringResourceId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        Color.Transparent
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = modifier
    ) {
        if (selected) {
            Icon(
                painter = painterResource(iconResourceId),
                contentDescription = stringResource(stringResourceId)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(text = stringResource(stringResourceId))
    }
}

private fun NavBackStack<NavKey>.navigateToTopLevel(destination: AppNavKey) {
    if (lastOrNull() == destination) return

    when (destination) {
        AppNavKey.TodoHome -> {
            while (size > 1) {
                removeLastOrNull()
            }
        }

        is AppNavKey.TodoList -> {
            removeAll { it is AppNavKey.TodoList }
            add(destination)
        }

        AppNavKey.TodoCalendar -> {
            removeAll { it == destination }
            add(destination)
        }

        else -> Unit
    }
}

private fun topLevelTransition(): ContentTransform =
    fadeIn(animationSpec = tween(durationMillis = 220)) togetherWith
            fadeOut(animationSpec = tween(durationMillis = 120))

private fun todoFormEnterTransition(): ContentTransform =
    (slideInVertically(
        animationSpec = tween(durationMillis = 300),
        initialOffsetY = { fullHeight -> fullHeight / 4 }
    ) + fadeIn(animationSpec = tween(durationMillis = 220))) togetherWith
            fadeOut(animationSpec = tween(durationMillis = 120))

private fun todoFormExitTransition(): ContentTransform =
    fadeIn(animationSpec = tween(durationMillis = 220)) togetherWith
            (slideOutVertically(
                animationSpec = tween(durationMillis = 260),
                targetOffsetY = { fullHeight -> fullHeight / 4 }
            ) + fadeOut(animationSpec = tween(durationMillis = 180)))
