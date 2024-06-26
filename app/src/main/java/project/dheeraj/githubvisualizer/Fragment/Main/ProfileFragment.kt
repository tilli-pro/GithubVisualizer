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

import RepositoryModel
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.firebase.auth.FirebaseAuth
import com.shreyaspatil.MaterialDialog.AbstractDialog
import com.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import project.dheeraj.githubvisualizer.Activity.FollowActivity
import project.dheeraj.githubvisualizer.Activity.LoginActivity
import project.dheeraj.githubvisualizer.Activity.RepositoriesActivity
import project.dheeraj.githubvisualizer.Adapter.RepositoryAdapter
import project.dheeraj.githubvisualizer.AppConfig
import project.dheeraj.githubvisualizer.R
import project.dheeraj.githubvisualizer.ViewModel.ProfileViewModel


class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var sharedPref: SharedPreferences
    private lateinit var token: String
    private lateinit var mainView: View
    private var starPage: Int = 1
    private var starRepo: ArrayList<RepositoryModel> = ArrayList()
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_profile, container, false)
        mainView = view
        sharedPref = context!!.getSharedPreferences(AppConfig.SHARED_PREF, Context.MODE_PRIVATE)
        token ="token ${sharedPref.getString(AppConfig.ACCESS_TOKEN, "")}"

        view.llFollowers.setOnClickListener {

            val intent = Intent(context, FollowActivity::class.java)
            intent.putExtra(AppConfig.LOGIN, sharedPref.getString(AppConfig.LOGIN, "User"))
            intent.putExtra("PAGE", "follower")
            startActivity(intent)

        }

        view.llFollowing.setOnClickListener {

            val intent = Intent(context, FollowActivity::class.java)
            intent.putExtra(AppConfig.LOGIN, sharedPref.getString(AppConfig.LOGIN, "User"))
            intent.putExtra("PAGE", "following")
            startActivity(intent)

        }

        view.llRepo.setOnClickListener {

            val intent = Intent(context, RepositoriesActivity::class.java)
            intent.putExtra(AppConfig.LOGIN, sharedPref.getString(AppConfig.LOGIN, "User"))
            intent.putExtra("USER_TYPE", "me")
            intent.putExtra("PAGE", "REPO")
            startActivity(intent)

        }


        view.llWebsite.setOnClickListener {
            val url = "${tvWebsite.text}"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        view.llEmail.setOnClickListener {
            val emailIntent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "${tvEmail.text}", null
                )
            )
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Body")
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }

        view.llGists.setOnClickListener {

            val intent = Intent(context, RepositoriesActivity::class.java)
            intent.putExtra(AppConfig.LOGIN, sharedPref.getString(AppConfig.LOGIN, "User"))
            intent.putExtra("USER_TYPE", "me")
            intent.putExtra("PAGE", "STARS")
            startActivity(intent)

        }

        view.llTwitter.setOnClickListener {
            var intent: Intent? = null
            try { // get the Twitter app if possible
                context!!.packageManager.getPackageInfo("com.twitter.android", 0)
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=${tvTwitter.text.toString()}"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            } catch (e: Exception) { // no Twitter app, revert to browser
                intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/${tvTwitter.text.toString()}")
                )
            }
            this.startActivity(intent)
        }


        // TODO
        view.profileImage.setOnClickListener {
//            DynamicToast.makeWarning(context!!, "Developing").show()
            shareUser()
        }

        firebaseAuth = FirebaseAuth.getInstance()

        view.buttonLogout.setOnClickListener {

            val pDialog = MaterialDialog.Builder(activity!!)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Confirm", AbstractDialog.OnClickListener { dialogInterface, which ->

                    Toast.makeText(context!!, "Confirm", Toast.LENGTH_SHORT).show()
                    if (firebaseAuth.currentUser != null) {
                        firebaseAuth.signOut()
                        sharedPref.edit().clear().apply()
                    }
                    startActivity(Intent(context!!, LoginActivity::class.java))
                    activity!!.finish()
                })
                .setNegativeButton("Cancel", AbstractDialog.OnClickListener { dialogInterface, which ->

                    Toast.makeText(context!!, "Cancel", Toast.LENGTH_SHORT).show()
                    dialogInterface.dismiss()
                })
                .setCancelable(true)
                .build()
            pDialog.show()
        }

        return view;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        getUserData()

        fetchStars()
        viewModel.getMyStarredRepo(token, starPage)

        getTopRepos()

    }

    private fun getTopRepos(){

        viewModel.getMyTopRepositories(token)

        viewModel.topRepositoryList.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
//                if (NotificationsProgressBar.visibility == View.VISIBLE)
//                    NotificationsProgressBar.visibility = View.GONE
            }
            else {
                recyclerViewTopRepo.adapter = RepositoryAdapter(context!!, it)
            }
        })

    }

    private fun getUserData(){

        viewModel.getLoginProfile(token)

        viewModel.userList.observe(viewLifecycleOwner, Observer {

            if (cardUserInfo.visibility == View.GONE)
                cardUserInfo.visibility = View.VISIBLE

            if (profileUserProgressBar.visibility == View.VISIBLE)
                profileUserProgressBar.visibility = View.GONE

            if (it.name.isNullOrEmpty())
                tvDisplayName.text = "Github User"
            else
                tvDisplayName.text = it.name

            if (it.email.isNullOrEmpty())
                llEmail.visibility = View.GONE
            else
                tvEmail.text = it.email

            if (it.bio.isNullOrEmpty())
                tvBio.visibility = View.GONE
            else
                tvBio.text = it.bio

            if (it.company.isNullOrEmpty())
                llOrganisations.visibility = View.GONE
            else
                tvOrganization.text = it.company

            if (it.twitter_username.isNullOrEmpty())
                llTwitter.visibility = View.GONE
            else
                tvTwitter.text = it.twitter_username

            if (it.blog.isNullOrEmpty())
                llWebsite.visibility = View.GONE
            else
                tvWebsite.text = it.blog

            if (it.location.isNullOrEmpty())
                llLocation.visibility = View.GONE
            else
                tvLocation.text = it.location

            tvUserName.text = it.login
            tvJoined.text = "Joined: ${it.created_at.subSequence(0, 10)}"
            tvFollowers.text = it.followers.toString()
            tvFollowing.text = it.following.toString()
            tvRepositories.text =
                (it.public_repos + it.total_private_repos).toString()

            sharedPref.edit()
                .putString(AppConfig.NAME, it.name)
                .putString(AppConfig.LOGIN, it.login)
                .apply()



            Glide.with(this)
                .load(it.avatar_url)
                .into(profileImage)

        })

    }

    private fun fetchStars() {

        viewModel.starList.observe(viewLifecycleOwner, Observer {
            if (it.size > 0) {
                starRepo.addAll(it)
                starPage++
                tvStars.text = "${starRepo.size}"
                viewModel.getMyStarredRepo(token, starPage)
            } else
                tvStars.text = "${starRepo.size}"
        })

    }

    fun shareUser() {

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check my Github profile github.com/${tvUserName.text}\n\nShared via *Github Visualizer App*\n " +
                    "https://play.google.com/store/apps/details?id=project.dheeraj.githubvisualizer")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)

    }


}
