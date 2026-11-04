package com.focuslauncher.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.focuslauncher.app.databinding.ActivitySelectAppsBinding

class SelectAppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectAppsBinding
    private lateinit var store: SessionStore
    private val selected = linkedSetOf<String>()
    private var allApps: List<AppInfo> = emptyList()
    private lateinit var adapter: CheckAppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SystemChrome.install(this, binding.root)
        store = SessionStore(this)

        selected.addAll(store.getAllowedPackages())

        adapter = CheckAppAdapter(selected) { updateCount() }
        binding.appsList.layoutManager = LinearLayoutManager(this)
        binding.appsList.adapter = adapter

        allApps = AppRepository.loadLaunchableApps(this)
        adapter.submit(allApps)
        updateCount()

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s?.toString()?.trim()?.lowercase().orEmpty()
                val filtered = if (q.isEmpty()) allApps
                else allApps.filter {
                    it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
                }
                adapter.submit(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.confirmButton.setOnClickListener {
            if (selected.isEmpty()) {
                Toast.makeText(this, R.string.home_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            store.saveSession(selected.toSet())
            setResult(RESULT_OK)
            finish()
        }
        binding.restoreButton.setOnClickListener {
            HomeEscape.openSystemHomeSettings(this)
        }
        binding.phoneButton.setOnClickListener {
            val dial = AppRepository.resolveDialerIntent(this)
            if (dial != null) startActivity(dial)
            else Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show()
        }
        binding.settingsButton.setOnClickListener {
            startActivity(AppRepository.resolveSettingsIntent())
        }

        // The old onBackPressed override ignored back when there was no session.
        // When a session already exists this callback stays disabled, so predictive
        // back and the edge gesture are not swallowed. With no session, back still
        // leaves this screen (redispatch) instead of being ignored. "恢复系统桌面"
        // does not depend on that gesture.
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(!store.hasActiveSession()) {
                override fun handleOnBackPressed() {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        SystemChrome.showSystemBars(this)
    }

    private fun updateCount() {
        binding.countText.text = getString(R.string.selected_count, selected.size)
    }
}
