/*
 * MIT License
 *
 * Copyright (c) 2020 Dheeraj Kotwani
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package project.dheeraj.githubvisualizer.Fragment.Main

import NotificationModel
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_follow.*
import kotlinx.android.synthetic.main.fragment_notification.*
import project.dheeraj.githubvisualizer.Adapter.NotificationsAdapter
import project.dheeraj.githubvisualizer.AppConfig
import project.dheeraj.githubvisualizer.R
import project.dheeraj.githubvisualizer.ViewModel.NotificationsViewModel


class NotificationsFragment : Fragment() {

    companion object {
        fun newInstance() = NotificationsFragment()
    }

    private lateinit var viewModel: NotificationsViewModel
    private lateinit var sharedPref: SharedPreferences
    private lateinit var adapter: NotificationsAdapter
    private lateinit var notificationsList: ArrayList<NotificationModel>
    private var page: Int = 1
    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        sharedPref = context!!.getSharedPreferences(AppConfig.SHARED_PREF, Context.MODE_PRIVATE)
        token = "token ${sharedPref.getString(AppConfig.ACCESS_TOKEN, "")}"

        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NotificationsViewModel::class.java)
        // TODO: Use the ViewModel


        page = 1
        notificationsList = ArrayList()
        viewModel.getNotifications(token, page)
//        NotificationsProgressBar.isRefreshing = true
        adapter = NotificationsAdapter(context!!, notificationsList)
        notificationRecyclerView.adapter = adapter

        Glide.with(this)
            .load(R.drawable.github_loader)
            .into(gitNotificationsProgressbar)


        viewModel.notificationsList.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                if (NotificationsProgressBar.isRefreshing)
                    NotificationsProgressBar.isRefreshing = false
                if(gitNotificationsProgressbar.visibility == View.VISIBLE)
                    gitNotificationsProgressbar.visibility = View.GONE
                buttonNotificationLoadMore.visibility = View.GONE
            } else {

                buttonNotificationLoadMore.isClickable = true
                buttonNotificationLoadMore.visibility = View.VISIBLE


                if (NotificationsProgressBar.isRefreshing)
                    NotificationsProgressBar.isRefreshing = false

//                notificationRecyclerView.adapter = NotificationsAdapter(context!!, it)
                notificationsList.addAll(it)
                adapter.notifyDataSetChanged()
                if(gitNotificationsProgressbar.visibility == View.VISIBLE)
                    gitNotificationsProgressbar.visibility = View.GONE
            }
        })


        NotificationsProgressBar.setOnRefreshListener {

            page = 1
            viewModel.getNotifications(token, page)

        }

        buttonNotificationLoadMore.setOnClickListener {

            page++
            buttonNotificationLoadMore.isClickable = false

            if (!NotificationsProgressBar.isRefreshing)
                NotificationsProgressBar.isRefreshing = true

            viewModel.getNotifications(token, page)

        }

    }

}
