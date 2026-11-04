package com.focuslauncher.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.focuslauncher.app.databinding.ActivityLauncherBinding

class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding
    private lateinit var store: SessionStore
    private lateinit var gridAdapter: GridAppAdapter
    private var allAppsCache: List<AppInfo> = emptyList()
    private var selectOpen = false
    private var userDismissedSelect = false
    private var explainedHomeBack = false

    private val selectLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        selectOpen = false
        userDismissedSelect = result.resultCode != RESULT_OK
        refreshUi()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemChrome.install(this, binding.root)
        store = SessionStore(this)
        userDismissedSelect = savedInstanceState?.getBoolean(STATE_DISMISSED_SELECT) == true

        gridAdapter = GridAppAdapter { app -> launchApp(app) }
        binding.appsGrid.layoutManager = GridLayoutManager(this, 4)
        binding.appsGrid.adapter = gridAdapter

        binding.phoneButton.setOnClickListener { openDialer() }
        binding.settingsButton.setOnClickListener {
            startActivity(AppRepository.resolveSettingsIntent())
        }
        binding.changeButton.setOnClickListener {
            if (store.hasActiveSession()) authenticateThenChange()
            else openSelect()
        }
        binding.exitButton.setOnClickListener {
            HomeEscape.openSystemHomeSettings(this)
        }
        binding.setDefaultButton.setOnClickListener {
            HomeEscape.openSystemHomeSettings(this)
        }

        // HOME has nowhere to go. Consume back here only — do not finish, or the
        // system just restarts this activity. Secondary screens finish themselves.
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!explainedHomeBack) {
                        explainedHomeBack = true
                        Toast.makeText(
                            this@LauncherActivity,
                            R.string.home_back_nowhere,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_DISMISSED_SELECT, userDismissedSelect)
    }

    override fun onResume() {
        super.onResume()
        SystemChrome.showSystemBars(this)
        refreshUi()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            SystemChrome.showSystemBars(this)
            maybeOpenSelect()
        }
    }

    private fun maybeOpenSelect() {
        if (store.hasActiveSession() || userDismissedSelect || selectOpen) return
        openSelect()
    }

    private fun openSelect() {
        if (selectOpen) return
        selectOpen = true
        userDismissedSelect = false
        try {
            selectLauncher.launch(Intent(this, SelectAppsActivity::class.java))
        } catch (_: Exception) {
            selectOpen = false
            Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshUi() {
        updateDefaultHint()
        binding.changeButton.setText(
            if (store.hasActiveSession()) R.string.change_list else R.string.choose_apps
        )
        if (!store.hasActiveSession()) {
            gridAdapter.submit(emptyList())
            binding.emptyText.visibility = View.VISIBLE
            return
        }
        val allowed = store.getAllowedPackages()
        if (allAppsCache.isEmpty()) {
            allAppsCache = AppRepository.loadLaunchableApps(this)
        }
        val byPkg = allAppsCache
            .filter { it.packageName in allowed }
            .distinctBy { it.packageName }
        gridAdapter.submit(byPkg)
        binding.emptyText.visibility = if (byPkg.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateDefaultHint() {
        val isDefault = isDefaultLauncher()
        binding.hintText.visibility = if (isDefault) View.GONE else View.VISIBLE
        binding.setDefaultButton.visibility = if (isDefault) View.GONE else View.VISIBLE
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val res = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return res?.activityInfo?.packageName == packageName
    }

    private fun launchApp(app: AppInfo) {
        val launch = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launch)
        } else {
            Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openDialer() {
        val dial = AppRepository.resolveDialerIntent(this)
        if (dial != null) startActivity(dial)
        else Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show()
    }

    private fun authenticateThenChange() {
        AuthHelper.authenticate(
            activity = this,
            title = getString(R.string.auth_title),
            subtitle = getString(R.string.auth_subtitle_change),
            onSuccess = { openSelect() },
            onError = { msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }
        )
    }

    companion object {
        private const val STATE_DISMISSED_SELECT = "dismissed_select"
    }
}
