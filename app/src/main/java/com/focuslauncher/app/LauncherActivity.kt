package com.focuslauncher.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.focuslauncher.app.databinding.ActivityLauncherBinding

class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding
    private lateinit var store: SessionStore
    private lateinit var gridAdapter: GridAppAdapter
    private var allAppsCache: List<AppInfo> = emptyList()

    private val selectLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshUi()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        store = SessionStore(this)

        gridAdapter = GridAppAdapter { app -> launchApp(app) }
        binding.appsGrid.layoutManager = GridLayoutManager(this, 4)
        binding.appsGrid.adapter = gridAdapter

        binding.phoneButton.setOnClickListener {
            val dial = AppRepository.resolveDialerIntent(this)
            if (dial != null) startActivity(dial)
            else Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show()
        }
        binding.settingsButton.setOnClickListener {
            startActivity(AppRepository.resolveSettingsIntent())
        }
        binding.changeButton.setOnClickListener { authenticateThenChange() }
        binding.exitButton.setOnClickListener { authenticateThenExit() }
        binding.setDefaultButton.setOnClickListener {
            try {
                startActivity(AppRepository.openDefaultAppsSettings(this))
            } catch (_: Exception) {
                startActivity(AppRepository.resolveHomeChooser(this))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ensureSessionOrSelect()
        refreshUi()
    }

    private fun ensureSessionOrSelect() {
        if (!store.hasActiveSession()) {
            openSelect(force = true)
        }
    }

    private fun openSelect(force: Boolean) {
        val intent = Intent(this, SelectAppsActivity::class.java)
        if (force) {
            // Clear any stale flags
        }
        selectLauncher.launch(intent)
    }

    private fun refreshUi() {
        updateDefaultHint()
        if (!store.hasActiveSession()) {
            gridAdapter.submit(emptyList())
            binding.emptyText.visibility = View.VISIBLE
            return
        }
        val allowed = store.getAllowedPackages()
        if (allAppsCache.isEmpty()) {
            allAppsCache = AppRepository.loadLaunchableApps(this)
        }
        // Prefer unique by package: show one launcher activity per allowed package
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

    private fun authenticateThenChange() {
        AuthHelper.authenticate(
            activity = this,
            title = getString(R.string.auth_title),
            subtitle = getString(R.string.auth_subtitle_change),
            onSuccess = { openSelect(force = false) },
            onError = { msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }
        )
    }

    private fun authenticateThenExit() {
        AuthHelper.authenticate(
            activity = this,
            title = getString(R.string.auth_title),
            subtitle = getString(R.string.auth_subtitle_exit),
            onSuccess = {
                // Open home chooser so user can pick another launcher / browse apps via Settings
                try {
                    startActivity(AppRepository.openDefaultAppsSettings(this))
                } catch (_: Exception) {
                    startActivity(AppRepository.resolveHomeChooser(this))
                }
            },
            onError = { msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }
        )
    }
}
