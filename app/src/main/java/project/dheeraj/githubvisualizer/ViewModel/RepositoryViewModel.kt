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

package project.dheeraj.githubvisualizer.ViewModel

import EventsModel
import Readme
import RepositoryModel
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import project.dheeraj.githubvisualizer.Model.RepositoryModel.RepositoryContentModel
import project.dheeraj.githubvisualizer.Network.GithubApiInterface
import project.dheeraj.githubvisualizer.Repository.NetworkRepository
import timber.log.Timber
import java.lang.Exception

class RepositoryViewModel: ViewModel() {

    private val repository = NetworkRepository(GithubApiInterface())

    private var mutableRepoData = MutableLiveData<RepositoryModel>()
    private var mutableStarData = MutableLiveData<Int>()
    private var mutableReadmeData = MutableLiveData<Readme>()
    private var mutableRepoContent = MutableLiveData<ArrayList<RepositoryContentModel>>()
    private var mutableRepoEvents = MutableLiveData<ArrayList<EventsModel>>()

    val repoData: LiveData<RepositoryModel>
    val starData: LiveData<Int>
    val readmeData: LiveData<Readme>
    val repoContent: LiveData<ArrayList<RepositoryContentModel>>
    val repoEvents: LiveData<ArrayList<EventsModel>>

    init {
        repoData = mutableRepoData
        starData = mutableStarData
        readmeData = mutableReadmeData
        repoContent = mutableRepoContent
        repoEvents = mutableRepoEvents
    }

    fun getStar (token: String, username:String, repo: String) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                mutableStarData.postValue(
                    repository.getStar(
                        token,
                        username,
                        repo
                    )
                )
            }
            catch (e: Exception) {
                Log.e("Star", e.message)
            }
        }
    }


    fun putStar (token: String, username:String, repo: String) {

        viewModelScope.launch(Dispatchers.Main) {
            try{
            mutableStarData.postValue(repository.putStar(
                token,
                username,
                repo
            ))
            }
            catch (e: Exception) {
                Log.e("Star", e.message)
            }
        }
    }

    fun removeStar (token: String, username:String, repo: String) {

        viewModelScope.launch(Dispatchers.Main) {
            try{
            mutableStarData.postValue(repository.deleteStar(
                token,
                username,
                repo
            ))
            }
            catch (e: Exception) {
                Log.e("Star", e.message)
            }
        }
    }

    fun repoDetails (token: String, username:String, repo: String) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
            mutableRepoData.postValue(repository.getRepoDetails(
                token,
                username,
                repo
            ))
            }
            catch (e: Exception) {
                Log.e("Repo", e.message)
            }
        }
    }

    fun getReadme (token: String, username:String, repo: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try{
                mutableReadmeData.postValue(repository.getRepoReadme(
                    token,
                    username,
                    repo
                ))
            }
            catch (e: Exception) {
                mutableRepoData.postValue(null)
                Log.e("Readme", e.message)
            }

        }
    }

    fun repoContent (token: String, username:String, repo: String, path: String) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                mutableRepoContent.postValue(
                    repository.getRepoContent(
                        token,
                        username,
                        repo,
                        path
                    )
                )
            }
            catch (e: Exception) {
                Log.e("Repo content", e.message)
            }
        }
    }

    fun repoEvents (token: String, owner:String, repo: String, page: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                mutableRepoEvents.postValue(
                    repository.getRepoEvents(
                        token,
                        owner,
                        repo,
                        page
                    )
                )
            }
            catch(e: Exception) {
                Log.e("Repo Events", e.message)
            }
        }
    }

}
