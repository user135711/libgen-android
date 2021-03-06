package com.viveksb007.libgenio;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by viveksb007 on 16/11/17.
 */

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.MyViewHolder> {

    private static final String TAG = "BooksAdapter";
    private String BASE_DOWNLOAD_URL = "http://libgen.io/get.php?md5=";
    private ArrayList<Book> bookList;
    private Context context;

    public BooksAdapter(Context context, ArrayList<Book> bookList) {
        this.bookList = bookList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Book book = bookList.get(position);
        holder.title.setText(book.getTitle());
        holder.author.setText("By : " + book.getAuthor());
        holder.fileSize.setText("Size : " + book.getSize());
        holder.fileExtension.setText("Format : " + book.getExtension());
        holder.pages.setText("Pages : " + book.getPages());
        holder.language.setText("Language : " + book.getLanguage());
        holder.year.setText("Year : " + book.getYear());
        holder.overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflowButton, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.book_menu, popup.getMenu());
        MyMenuItemClickListener itemClickListener = new MyMenuItemClickListener(position);
        popup.setOnMenuItemClickListener(itemClickListener);
        popup.show();
    }

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        private int position;

        public MyMenuItemClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_download:
                    Toast.makeText(context, "Download Book", Toast.LENGTH_SHORT).show();
                    downloadBook(position);
                    return true;
                case R.id.action_share:
                    Toast.makeText(context, "Share Book Link", Toast.LENGTH_SHORT).show();
                    shareLink(position);
                    return true;
            }
            return false;
        }
    }

    private void shareLink(int position) {

    }

    private void downloadBook(int position) {
        final String downloadLink = bookList.get(position).getDownloadLink();
        final String fileName = bookList.get(position).getTitle() + '.' + bookList.get(position).getExtension();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.v(TAG, downloadLink);
                    Document doc = Jsoup.connect(downloadLink).get();
                    if (doc == null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Error in fetching data.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    Elements elements = doc.getElementsByTag("td");
                    String finalDownloadLink = "";
                    for (Element element : elements) {
                        if ("center".equals(element.attr("align")) && "top".equals(element.attr("valign"))) {
                            finalDownloadLink = (element.children()).get(0).attr("href");
                            break;
                        }
                    }
                    if ("".equals(finalDownloadLink)) {
                        Log.v(TAG, "Link not found");
                    } else {
                        Log.v(TAG, finalDownloadLink);
                        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(finalDownloadLink));
                        File destinationFile = new File(Environment.getExternalStorageDirectory(), fileName);
                        downloadRequest.setDescription("Downloading Book");
                        downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                        downloadRequest.setDestinationUri(Uri.fromFile(destinationFile));
                        assert downloadManager != null;
                        downloadManager.enqueue(downloadRequest);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView title, author, pages, fileExtension, fileSize, language, year;
        public ImageButton overflowButton;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_title);
            author = view.findViewById(R.id.tv_author);
            pages = view.findViewById(R.id.tv_pages);
            fileExtension = view.findViewById(R.id.tv_file_extension);
            fileSize = view.findViewById(R.id.tv_file_size);
            overflowButton = view.findViewById(R.id.overflow_button);
            language = view.findViewById(R.id.tv_language);
            year = view.findViewById(R.id.tv_year);
        }

    }
}
