package fr.pamv.pamvadmin;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.pamv.pamvadmin.entities.Article;

public class ArticleViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.title_card)
    TextView title;

    @BindView(R.id.content_card)
    TextView content;

    @BindView(R.id.image_card)
    ImageView imageView;
    
    private WeakReference<ArticleAdapter.Listener> callbackWeakRef;

    public ArticleViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Article article, ArticleAdapter.Listener callback)
    {
        title.setText(article.getTitle());
        content.setText(Html.fromHtml(article.getIntro()));
        Picasso.get().load(Utils.URL_SITE + article.getImage())
                .fit().centerCrop()
                .into(imageView);
        callbackWeakRef = new WeakReference<>(callback);
    }
    
    @OnClick(R.id.delete_card)
    public void delete()
    {
        ArticleAdapter.Listener callback = callbackWeakRef.get();
        if (callback != null)
            callback.onClickDeleteButton(getAdapterPosition());
    }
}
