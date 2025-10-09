package com.example.githublistapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load

class MainActivity : ComponentActivity() {

    private lateinit var recycler: RecyclerView
    private val adapter = UserAdapter()

    private val api = APIClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        load()
    }

    private fun load() {
        showLoading(true)
        Thread {
            val result = api.fetchUsers(since = 0, perPage = 30)
            val ui = result.getOrElse { emptyList() }.map {
                UserUi(
                    name = it.login ?: "(no name)",
                    url = it.html_url.orEmpty(),
                    avatar = it.avatar_url.orEmpty()
                )
            }
            runOnUiThread {
                adapter.submit(ui)
                showLoading(false)
            }
        }.start()
    }

    private fun showLoading(show: Boolean) {
        val hud = findViewById<View>(R.id.loading)
        if (show) {
            hud.alpha = 0f
            hud.isVisible = true
            hud.animate().alpha(1f).setDuration(120).start()
        } else {
            hud.animate().alpha(0f).setDuration(120).withEndAction { hud.isVisible = false }.start()
        }
    }

    data class UserUi(val name: String, val url: String, val avatar: String)

    inner class UserAdapter : RecyclerView.Adapter<UserVH>() {
        private val data = mutableListOf<UserUi>()
        fun submit(newData: List<UserUi>) { data.clear(); data.addAll(newData); notifyDataSetChanged() }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user, parent, false) as ViewGroup
            return UserVH(v)
        }
        override fun onBindViewHolder(holder: UserVH, position: Int) = holder.bind(data[position])
        override fun getItemCount(): Int = data.size
    }

    inner class UserVH(private val root: ViewGroup) : RecyclerView.ViewHolder(root) {
        private val img: ImageView = root.findViewById(R.id.imgAvatar)
        private val name: TextView = root.findViewById(R.id.txtName)
        private val url : TextView = root.findViewById(R.id.txtUrl)
        fun bind(item: UserUi) {
            name.text = item.name
            url.text  = item.url
            img.load(item.avatar)
        }
    }
}