package com.example.hifdhpartner.OtherScreens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.DAY_OF_YEAR
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import java.util.Calendar.MONTH
import java.util.Calendar.SECOND
import java.util.Calendar.SUNDAY
import java.util.Calendar.WEEK_OF_YEAR
import java.util.concurrent.TimeUnit
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import com.example.hifdhpartner.Helpers.DailyNotificationWorker
import com.example.hifdhpartner.Helpers.DataStoreManager
import com.example.hifdhpartner.Helpers.MonthlyNotificationWorker
import com.example.hifdhpartner.ViewModel
import com.example.hifdhpartner.Helpers.WeeklyNotificationWorker


@Composable
fun Goals(navController: NavController, viewModel: ViewModel) {
    val userData by viewModel.userData.collectAsState()
    val context = LocalContext.current
    val notificationsEnabledFlow = remember { DataStoreManager.getNotificationsEnabled(context) }
    val notificationsEnabled by notificationsEnabledFlow.collectAsState(initial = false)

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp), // prevent overlapping with bottom bar
        ) {
            item {
                MainText("Goals")
                Spacer(modifier = Modifier.height(16.dp))
            }

            userData?.let { data ->
                item {
                    GoalInputField(
                        label = "Daily Memorization Goal",
                        value = data.dailyMemorizationGoal,
                        onSave = { viewModel.updateDailyMemorizationGoal(it) }
                    )
                }
                item {
                    GoalInputField(
                        label = "Daily Revision Goal",
                        value = data.dailyRevisionGoal,
                        onSave = { viewModel.updateDailyRevisionGoal(it) }
                    )
                }
                item {
                    GoalInputField(
                        label = "Weekly Memorization Goal",
                        value = data.weeklyMemorizationGoal,
                        onSave = { viewModel.updateWeeklyMemorizationGoal(it) }
                    )
                }
                item {
                    GoalInputField(
                        label = "Weekly Revision Goal",
                        value = data.weeklyRevisionGoal,
                        onSave = { viewModel.updateWeeklyRevisionGoal(it) }
                    )
                }
                item {
                    GoalInputField(
                        label = "Monthly Memorization Goal",
                        value = data.monthlyMemorizationGoal,
                        onSave = { viewModel.updateMonthlyMemorizationGoal(it) }
                    )
                }
                item {
                    GoalInputField(
                        label = "Monthly Revision Goal",
                        value = data.monthlyRevisionGoal,
                        onSave = { viewModel.updateMonthlyRevisionGoal(it) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Notifications",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { isChecked ->
                            viewModel.viewModelScope.launch {
                                DataStoreManager.setNotificationsEnabled(context, isChecked)
                                if (isChecked) {
                                    scheduleNotifications(context)
                                    Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
                                } else {
                                    disableNotifications(context)
                                    Toast.makeText(context, "Notifications disabled", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}





@Composable
fun GoalInputField(
    label: String,
    value: String,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(value) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                singleLine = true,
                textStyle = TextStyle(color = Color.Black),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { onSave(text) } // Save on Enter
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onSave(text) },
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}




fun scheduleNotifications(context: Context) {
    val workManager = WorkManager.getInstance(context)

    // Daily Notification
    val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(calculateInitialDelay(8, 0), TimeUnit.MILLISECONDS)
        .build()
    workManager.enqueueUniquePeriodicWork("daily_goals_notification", ExistingPeriodicWorkPolicy.REPLACE, dailyWorkRequest)

    // Weekly Notification (Every Sunday at 8:00 AM)
    val weeklyWorkRequest = PeriodicWorkRequestBuilder<WeeklyNotificationWorker>(7, TimeUnit.DAYS)
        .setInitialDelay(calculateInitialDelayForDay(SUNDAY, 8, 0), TimeUnit.MILLISECONDS)
        .build()
    workManager.enqueueUniquePeriodicWork("weekly_goals_notification", ExistingPeriodicWorkPolicy.REPLACE, weeklyWorkRequest)

    // Monthly Notification (28th of each month at 8:00 AM)
    val monthlyWorkRequest = PeriodicWorkRequestBuilder<MonthlyNotificationWorker>(30, TimeUnit.DAYS)
        .setInitialDelay(calculateInitialDelayForDate(28, 8, 0), TimeUnit.MILLISECONDS)
        .build()
    workManager.enqueueUniquePeriodicWork("monthly_goals_notification", ExistingPeriodicWorkPolicy.REPLACE, monthlyWorkRequest)
}

fun calculateInitialDelay(hour: Int, minute: Int): Long {
    val now = java.util.Calendar.getInstance()
    val target = now.clone() as Calendar
    target.set(HOUR_OF_DAY, hour)
    target.set(MINUTE, minute)
    target.set(SECOND, 0)
    if (now.after(target)) target.add(DAY_OF_YEAR, 1)

    return target.timeInMillis - now.timeInMillis
}

fun calculateInitialDelayForDay(dayOfWeek: Int, hour: Int, minute: Int): Long {
    val now = java.util.Calendar.getInstance()
    val target = now.clone() as Calendar
    target.set(DAY_OF_WEEK, dayOfWeek)
    target.set(HOUR_OF_DAY, hour)
    target.set(MINUTE, minute)
    target.set(SECOND, 0)
    if (now.after(target)) target.add(WEEK_OF_YEAR, 1)

    return target.timeInMillis - now.timeInMillis
}

fun calculateInitialDelayForDate(dayOfMonth: Int, hour: Int, minute: Int): Long {
    val now = java.util.Calendar.getInstance()
    val target = now.clone() as Calendar
    target.set(DAY_OF_MONTH, dayOfMonth)
    target.set(HOUR_OF_DAY, hour)
    target.set(MINUTE, minute)
    target.set(SECOND, 0)
    if (now.after(target)) target.add(MONTH, 1)

    return target.timeInMillis - now.timeInMillis
}

fun disableNotifications(context: Context) {
    val workManager = WorkManager.getInstance(context)
    workManager.cancelUniqueWork("daily_goals_notification")
    workManager.cancelUniqueWork("weekly_goals_notification")
    workManager.cancelUniqueWork("monthly_goals_notification")
}


