package com.example.kotlin_btk_09.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin_btk_09.databinding.RecyclerRowBinding
import com.example.kotlin_btk_09.model.Post
import com.squareup.picasso.Picasso

class PostAdapter(private  val postList : ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>()       {

    class  PostViewHolder(val binding : RecyclerRowBinding): RecyclerView.ViewHolder(binding.root)  {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.PostViewHolder {

        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return  PostViewHolder(binding)

   }


    override fun getItemCount(): Int {
        return  postList.size;
    }

    override fun onBindViewHolder(holder: PostAdapter.PostViewHolder, position: Int) {

        holder.binding.recyclerEmailText.text = postList[position].email
        holder.binding.recyclerCommentText.text = postList[position].comment
        Picasso.get().load(postList[position].downloadUrl).into(holder.binding.recyclerImageView)

    }
}
