package com.Project.Closet.social.subfragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.Project.Closet.Global;
import com.Project.Closet.HTTP.Service.SocialService;
import com.Project.Closet.HTTP.Session.preference.MySharedPreferences;
import com.Project.Closet.HTTP.VO.DetailFeedVO;
import com.Project.Closet.HTTP.VO.FeedVO;
import com.Project.Closet.HTTP.VO.HeartVO;
import com.Project.Closet.R;
import com.Project.Closet.social.detailFeed.activity_thisFeed;
import com.Project.Closet.social.space.activity_space;
import com.Project.Closet.util.NumFormat;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;


//어댑터 : 리사이클러뷰의 아이템 뷰를 생성하는 역할을 함
//뷰 홀더 : 아이템 뷰를 저장하는 객체
//아이템 뷰 : 각각의 카드뷰 한 개
public class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.ViewHolder> {

    Context context;
    ArrayList<FeedVO> feedList; // 피드 리스트

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListener mListener = null ;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }

    //생성자에서 데이터 리스트 객체를 전달받음.
    public FeedRecyclerAdapter(ArrayList<FeedVO> items) {
        this.feedList=items;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public FeedRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.item_cardview_feed, parent, false) ;
        FeedRecyclerAdapter.ViewHolder vh = new FeedRecyclerAdapter.ViewHolder(view);

        return vh;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(FeedRecyclerAdapter.ViewHolder holder, int position) {
        final FeedVO feedInfo = feedList.get(position);

        //작성 시간 포매팅
        long ts = Timestamp.valueOf(feedInfo.getRegDate()).getTime();
        String regDate = NumFormat.formatTimeString(ts);
        //수 포매팅
        String numHeart = NumFormat.formatNumString(feedInfo.getNumHeart());
        String numComment  = NumFormat.formatNumString(feedInfo.getNumComment());


        holder.tv_writerName.setText(feedInfo.getWriterName());
        holder.tv_numHeart.setText(numHeart); //형식 : 223.7만
        holder.tv_numComment.setText(numComment); //형식 : 223.7만
        holder.tv_contents.setText(feedInfo.getContents());
        holder.tv_regDate.setText(regDate); //형식 : 6시간 전

        Glide.with(holder.itemView.getContext()).load(Global.baseURL+feedInfo.getPfImagePath()).into(holder.iv_profileImage);
        Glide.with(holder.itemView.getContext()).load(Global.baseURL+feedInfo.getImagePath()).into(holder.iv_image);

        //하트 여부에 따라 아이콘 변경
        String if_hearting = feedInfo.getIf_hearting();
        if(if_hearting.contains("hearting")){
            holder.iv_heart.setImageResource(R.drawable.heart_color);
        }
        else if(if_hearting.contains("not_hearting")){
            holder.iv_heart.setImageResource(R.drawable.heart_empty);
        }

    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return this.feedList.size();
    }

    //뷰 홀더 : 아이템 뷰를 저장하는 객체
    public class ViewHolder extends RecyclerView.ViewHolder {


        TextView tv_writerName, tv_numHeart, tv_numComment, tv_contents, tv_regDate;
        ImageView iv_profileImage, iv_image, iv_heart, iv_comment, iv_addToCloset;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            tv_writerName = itemView.findViewById(R.id.tv_writerName);
            tv_numHeart = itemView.findViewById(R.id.tv_numHeart);
            tv_numComment = itemView.findViewById(R.id.tv_numComment);
            tv_contents = itemView.findViewById(R.id.tv_contents);
            tv_regDate = itemView.findViewById(R.id.tv_regDate);

            iv_profileImage = itemView.findViewById(R.id.iv_profileImage);
            iv_image = itemView.findViewById(R.id.iv_image);
            iv_heart = itemView.findViewById(R.id.iv_heart);
            iv_comment = itemView.findViewById(R.id.iv_comment);
            iv_addToCloset = itemView.findViewById(R.id.iv_addToCloset);

            //아이템 클릭 이벤트 처리
            View.OnClickListener onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    if (pos != RecyclerView.NO_POSITION) {
                        // TODO : use pos.
                        Intent intent;
                        String result=null;
                        context = v.getContext();
                        FeedVO feed = feedList.get(pos);

                        MySharedPreferences pref = MySharedPreferences.getInstanceOf(context);
                        String myID = pref.getUserID();


                        switch (v.getId()) {
                            case R.id.profile_area :
                                intent = new Intent(context, activity_space.class);
                                assert feed != null;
                                intent.putExtra("targetID", feed.getWriterID());
                                context.startActivity(intent);
                                break;
                            case R.id.ll_icon_heart :
                                try {
                                    result = new heartTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Integer.toString(feed.getBoardNo()) ,myID).get();
                                } catch (ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if(result!=null) {
                                    String resCut[];
                                    String numHeart;
                                    if("fail".equals(result)){

                                    }else{
                                        if(result.contains("not_hearting")){
                                            iv_heart.setImageResource(R.drawable.heart_empty);
                                            resCut = result.split("_");
                                            numHeart = NumFormat.formatNumString(Integer.parseInt(resCut[0])); //수 포매팅
                                            tv_numHeart.setText(numHeart);
                                        }else if(result.contains("hearting")) {
                                            iv_heart.setImageResource(R.drawable.heart_color);
                                            resCut = result.split("_");
                                            numHeart = NumFormat.formatNumString(Integer.parseInt(resCut[0])); //수 포매팅
                                            tv_numHeart.setText(numHeart);
                                        }
                                    }
                                }

                                break;
                            default :
                                ArrayList<DetailFeedVO> selectedFeedList=new ArrayList<DetailFeedVO>();
                                try {
                                    selectedFeedList = new detailInfoTask().execute(Integer.toString(feed.getBoardNo())).get();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }


                                intent = new Intent(context, activity_thisFeed.class);


                                assert feed != null;
                                intent.putParcelableArrayListExtra("selectedFeedList", selectedFeedList);

                                context.startActivity(intent);
                        }

                        if(mListener!=null) {
                            //mListener.onItemClick(v,pos);
                        }
                    }
                }
            };

            CardView feed_card = itemView.findViewById(R.id.feed_card);
            LinearLayout profile_area = itemView.findViewById(R.id.profile_area);
            LinearLayout ll_icon_heart = itemView.findViewById(R.id.ll_icon_heart);

            feed_card.setOnClickListener(onClickListener);
            profile_area.setOnClickListener(onClickListener);
            ll_icon_heart.setOnClickListener(onClickListener);

        }
    }

    public class detailInfoTask extends AsyncTask<String, Void, ArrayList<DetailFeedVO>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            startTime = Util.getCurrentTime();
        }


        @Override
        protected ArrayList<DetailFeedVO> doInBackground(String... params) {
            String myID = MySharedPreferences.getInstanceOf(context).getUserID();

            Call<List<DetailFeedVO>> feedListCall = SocialService.getRetrofit(context).detailFeed(params[0],myID);
            //인자 params[0]은 클릭한 boardNo.


            try {
                return (ArrayList<DetailFeedVO>) feedListCall.execute().body();

                // Do something with the response.
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<DetailFeedVO> feeds) {
            super.onPostExecute(feeds);
        }
    }




    public class heartTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            startTime = Util.getCurrentTime();
        }


        @Override
        protected String doInBackground(String... params) {
            HeartVO heartInfo = new HeartVO(params[0],params[1]);
            //params : 게시물 번호, 유저

            Call<String> stringCall = SocialService.getRetrofit(context).executeHeart(heartInfo);

            //인자 params[0]은 page.

            try {
                return stringCall.execute().body();

                // Do something with the response.
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);
        }
    }



}