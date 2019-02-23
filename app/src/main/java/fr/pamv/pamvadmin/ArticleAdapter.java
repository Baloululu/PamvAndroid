package fr.pamv.pamvadmin;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fr.pamv.pamvadmin.entities.Article;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleViewHolder> {

    public interface Listener{
        void onClickDeleteButton(int position);
    }

    List<Article> articles;
    private final Listener callback;

    public ArticleAdapter(List<Article> articles, Listener callback) {
        this.articles = articles;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_cards, viewGroup, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder articleViewHolder, int i) {
        Article article = articles.get(i);
        articleViewHolder.bind(article, this.callback);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }
}
