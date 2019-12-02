package com.example.newsgateway;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ArticlesFragment extends Fragment {

    private static final String TAG = "ArticlesFragment";

    TextView headline, author, date, text, count;
    ImageView photo;
    Article article;
    int counter;
    View view;


    private static final String ARTICLE = "ARTICLE";
    private static final String INDEX = "INDEX";
    private static final String TOTAL = "TOTAL";



    public ArticlesFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static final ArticlesFragment newInstance(Article article, int index, int total) {
        ArticlesFragment fragment = new ArticlesFragment();
        Bundle args = new Bundle(1);
        args.putSerializable(ARTICLE, article);
        args.putInt(INDEX, index);
        args.putInt(TOTAL, total);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        article = (Article) (getArguments() != null ? getArguments().getSerializable(ARTICLE) : null);
        counter = getArguments().getInt(INDEX) + 1;
        int total = getArguments().getInt(TOTAL);
        String lastLine = counter + " of " + total;

        view = inflater.inflate(R.layout.fragment_articles, container, false);

        headline = view.findViewById(R.id.article_headline);
        date = view.findViewById(R.id.article_date);
        author = view.findViewById(R.id.article_author);
        text = view.findViewById(R.id.article_text);
        count = view.findViewById(R.id.article_count);
        photo = view.findViewById(R.id.article_photo);


        count.setText(lastLine);
        if (article.getTitle() != null) {
            headline.setText(article.getTitle());
        } else {
            headline.setText("");
        }

        if (article.getAuthor() != null) {
            author.setText(article.getAuthor());
        } else {
            author.setText("Unknown");
        }

        if (article.getPublishedAt() != null && !article.getPublishedAt().isEmpty()) {
            String stringDate = article.getPublishedAt();
            Date date = null;
            String public_date;
            try {
                if (stringDate != null) {
                    date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(stringDate);
                }
                String pattern = "MMM dd, yyyy HH:mm";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                public_date = simpleDateFormat.format(date);
                this.date.setText(public_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        if (article.getDescription() != null) {
            text.setText(article.getDescription());
        } else {
            text.setText("");
        }

        author.setMovementMethod(new ScrollingMovementMethod());
        if (article.getUrlToImage() != null) {
            loadRemoteImage(article.getUrlToImage());
        }

        headline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNews();
            }
        });

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNews();
            }
        });

        author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNews();
            }
        });

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNews();
            }
        });


        return view;


    }
    private void loadRemoteImage(final String imageURL) {

        if (imageURL != null) {
            Picasso picasso = new Picasso.Builder(getActivity()).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    final String changedUrl = imageURL.replace("http:", "https:");
                    picasso.load(changedUrl)
                            .fit()
                            .centerCrop()
                            .error(R.drawable.notfound)
                            .placeholder(R.drawable.placeholder)
                            .into(photo);
                }
            }).build();
            picasso.load(imageURL)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.notfound)
                    .placeholder(R.drawable.placeholder)
                    .into(photo);
        } else {
            Picasso.with(getActivity()).load(imageURL)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.notfound)
                    .placeholder(R.drawable.placeholder)
                    .into(photo);
        }
    }


    private void goToNews() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(article.getUrl()));
        startActivity(intent);
    }

}
