package com.softvision.unittestingstudygroup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.softvision.unittestingstudygroup.exercise5.Album
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = AlbumAdapter()

        albums.adapter = adapter
        //TODO: submit items into adapter

        refresh.setOnRefreshListener {
            //TODO: refresh albums list
        }

        //TODO: show isRefreshing

        //TODO: show toast with error messages
    }
}

class AlbumAdapter : ListAdapter<Album, AlbumAdapter.AlbumViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_album, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(album: Album) {
            (itemView as TextView).text = album.title
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Album, newItem: Album) = oldItem == newItem

    }
}