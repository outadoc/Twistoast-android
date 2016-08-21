/*
 * Twistoast - ActivityMain
 * Copyright (C) 2013-2016 Baptiste Candellier
 *
 * Twistoast is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twistoast is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.twistoast

import android.app.Fragment
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import fr.outadev.android.transport.timeo.TimeoRequestHandler
import fr.outadev.android.transport.timeo.TimeoTrafficAlert
import fr.outadev.twistoast.background.BackgroundTasksManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_toolbar.*
import kotlinx.android.synthetic.main.view_traffic_alert.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * The main activity of the app.

 * @author outadoc
 */
class ActivityMain : ThemedActivity(), IStopsListContainer, NavigationView.OnNavigationItemSelectedListener {

    private var drawerToggle: ActionBarDrawerToggle? = null
    private var currentDrawerItem = 0
    private val loadedFragments: SparseArray<Fragment>

    private var trafficAlert: TimeoTrafficAlert? = null
    private val requestHandler: TimeoRequestHandler

    init {
        requestHandler = TimeoRequestHandler()
        loadedFragments = SparseArray<Fragment>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar
        setSupportActionBar(toolbar)

        // Drawer config
        drawerToggle = object : ActionBarDrawerToggle(this, navigationDrawer, toolbar, R.string.drawer_action_open, R.string.drawer_action_close) {

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
            }

            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
            }
        }

        navigationDrawer.addDrawerListener(drawerToggle!!)

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this)
        }

        // Handle saved variables and check traffic info if needed
        if (savedInstanceState != null) {
            currentDrawerItem = savedInstanceState.getInt("key_current_drawer_item")
            trafficAlert = savedInstanceState.get("key_traffic_alert") as TimeoTrafficAlert?
            displayGlobalTrafficInfo()
        } else {
            currentDrawerItem = DEFAULT_DRAWER_ITEM
            loadFragmentForDrawerItem(currentDrawerItem)
            checkForGlobalTrafficInfo()
        }

        // Turn the notifications back off if necessary
        val db = Database(DatabaseOpenHelper.getInstance(this))
        val config = ConfigurationManager(this)

        if (db.watchedStopsCount > 0) {
            BackgroundTasksManager.enableStopAlarmJob(applicationContext)
        }

        if (config.trafficNotificationsEnabled) {
            BackgroundTasksManager.enableTrafficAlertJob(applicationContext)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        val frag = loadedFragments[currentDrawerItem]

        if (frag is FragmentWebView && frag.canGoBack) {
            // If we can move back of a page in the browser, do it
            frag.goBack()
        } else if (currentDrawerItem == DEFAULT_DRAWER_ITEM) {
            // If we're on the main screen, just exit
            super.onBackPressed()
        } else if (!navigationDrawer.isDrawerOpen(GravityCompat.START)) {
            // If the drawer isn't opened, open it
            navigationDrawer.openDrawer(GravityCompat.START)
        } else {
            // Otherwise, go back to the main screen
            loadFragmentForDrawerItem(DEFAULT_DRAWER_ITEM)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("key_current_drawer_item", currentDrawerItem)
        outState.putSerializable("key_traffic_alert", trafficAlert)
    }

    override fun onStart() {
        super.onStart()
        displayGlobalTrafficInfo()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return drawerToggle!!.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    override fun endRefresh(success: Boolean) {
    }

    override val isRefreshing: Boolean
        get() = false

    override fun setNoContentViewVisible(visible: Boolean) {
    }

    override fun loadFragmentForDrawerItem(itemId: Int) {
        currentDrawerItem = itemId

        val fragmentToOpen = loadedFragments[itemId] ?: FragmentFactory.getFragmentFromMenuItem(this, itemId)!!
        loadedFragments.put(itemId, fragmentToOpen)

        // Insert the fragment by replacing any existing fragment
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragmentToOpen).commit()

        // Highlight the selected item, update the title, and close the drawer
        refreshActionBarTitle()
        navigationView.setCheckedItem(currentDrawerItem)
        navigationDrawer.closeDrawer(GravityCompat.START)
    }


    fun refreshActionBarTitle() {
        val item = navigationView.menu.findItem(currentDrawerItem) ?: return
        supportActionBar!!.title = item.title
    }

    /**
     * Fetches and stores a global traffic info if there is one available.
     */
    private fun checkForGlobalTrafficInfo() {
        doAsync() {
            val alert = requestHandler.globalTrafficAlert

            uiThread {
                trafficAlert = alert
                displayGlobalTrafficInfo()
            }
        }
    }

    /**
     * Displays a global traffic info if one was downloaded by checkForGlobalTrafficInfo.
     */
    private fun displayGlobalTrafficInfo() {
        if (trafficAlert != null && trafficAlertContainer != null && trafficAlertMessage != null) {
            Log.i(TAG, trafficAlert.toString())
            val url = trafficAlert?.url

            trafficAlertContainer.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }

            trafficAlertMessage.text = trafficAlert?.label?.replace("Info Trafic", "")?.trim({ it <= ' ' })
            trafficAlertMessage.isSelected = true
            trafficAlertContainer.visibility = View.VISIBLE

            // Set view_toolbar elevation to 0, since we'll have the traffic alert just right under it
            supportActionBar!!.elevation = 0f
        } else if (trafficAlertContainer != null) {
            trafficAlertContainer.visibility = View.GONE

            // Set view_toolbar elevation to 4 dp, not 4 px
            val pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics)
            supportActionBar!!.elevation = pixels
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        loadFragmentForDrawerItem(menuItem.itemId)
        return true
    }

    companion object {
        private val TAG = ActivityMain::class.java.simpleName
        private val DEFAULT_DRAWER_ITEM = R.id.drawer_realtime
    }
}
