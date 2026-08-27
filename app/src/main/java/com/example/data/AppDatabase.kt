package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.AppLimitRule
import com.example.model.BottomSlot
import com.example.model.CustomAppLabel
import com.example.model.FocusProfile
import com.example.model.HiddenApp
import com.example.model.TaskItem
import com.example.model.WidgetSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FocusProfile::class,
        AppLimitRule::class,
        TaskItem::class,
        HiddenApp::class,
        CustomAppLabel::class,
        BottomSlot::class,
        WidgetSlot::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun launcherDao(): LauncherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "minimalist_launcher.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.launcherDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: LauncherDao) {
                // Prepopulate default Focus Profiles
                dao.insertFocusProfile(
                    FocusProfile(
                        id = 1,
                        name = "Deep Work",
                        isDndLinked = false,
                        lockPrivateSpace = true,
                        requiresPrivateSpace = false,
                        appPackage1 = "com.google.android.gm",
                        appPackage2 = "com.google.android.keep",
                        appPackage3 = "com.google.android.calendar",
                        appPackage4 = "com.android.chrome",
                        appPackage5 = "com.google.android.apps.docs"
                    )
                )
                dao.insertFocusProfile(
                    FocusProfile(
                        id = 2,
                        name = "Minimal",
                        isDndLinked = false,
                        lockPrivateSpace = true,
                        requiresPrivateSpace = false,
                        appPackage1 = "com.google.android.dialer",
                        appPackage2 = "com.google.android.apps.messaging",
                        appPackage3 = "com.google.android.keep",
                        appPackage4 = "",
                        appPackage5 = ""
                    )
                )
                dao.insertFocusProfile(
                    FocusProfile(
                        id = 3,
                        name = "Evening & Wind Down",
                        isDndLinked = true,
                        lockPrivateSpace = true,
                        requiresPrivateSpace = false,
                        appPackage1 = "com.google.android.apps.books",
                        appPackage2 = "com.google.android.apps.podcasts",
                        appPackage3 = "com.google.android.deskclock",
                        appPackage4 = "",
                        appPackage5 = ""
                    )
                )
                dao.insertFocusProfile(
                    FocusProfile(
                        id = 4,
                        name = "Private Vault",
                        isDndLinked = false,
                        lockPrivateSpace = false,
                        requiresPrivateSpace = true,
                        appPackage1 = "",
                        appPackage2 = "",
                        appPackage3 = "",
                        appPackage4 = "",
                        appPackage5 = ""
                    )
                )

                // Prepopulate default sample tasks
                dao.insertTask(TaskItem(title = "Welcome to Minimalist Launcher", isDone = false, timestamp = System.currentTimeMillis()))
                dao.insertTask(TaskItem(title = "Swipe left to check utility & screen time", isDone = false, timestamp = System.currentTimeMillis() - 1000))
                dao.insertTask(TaskItem(title = "Swipe right to open all apps drawer", isDone = false, timestamp = System.currentTimeMillis() - 2000))
                dao.insertTask(TaskItem(title = "Tap weather slot to attach a 3rd party widget", isDone = false, timestamp = System.currentTimeMillis() - 3000))

                // Prepopulate default 4 bottom slots (Phone, Messages, Camera, Settings)
                dao.insertBottomSlot(BottomSlot(slotIndex = 0, packageName = "", customLabel = "Phone", defaultType = "phone", iconName = "phone"))
                dao.insertBottomSlot(BottomSlot(slotIndex = 1, packageName = "", customLabel = "Messages", defaultType = "messages", iconName = "messages"))
                dao.insertBottomSlot(BottomSlot(slotIndex = 2, packageName = "", customLabel = "Camera", defaultType = "camera", iconName = "camera"))
                dao.insertBottomSlot(BottomSlot(slotIndex = 3, packageName = "", customLabel = "Settings", defaultType = "settings", iconName = "settings"))

                // Prepopulate widget slots
                dao.insertWidgetSlot(WidgetSlot(slotKey = "HEADER_WEATHER", appWidgetId = -1, isCustomWidgetEnabled = false))
                dao.insertWidgetSlot(WidgetSlot(slotKey = "UTILITY_BATTERY", appWidgetId = -1, isCustomWidgetEnabled = false))
            }
        }
    }
}
