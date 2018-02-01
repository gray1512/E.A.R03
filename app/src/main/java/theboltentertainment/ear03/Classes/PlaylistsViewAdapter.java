package theboltentertainment.ear03.Classes;

import android.content.Context;
import android.content.Intent;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import theboltentertainment.ear03.MainActivity;
import theboltentertainment.ear03.Objects.Playlist;
import theboltentertainment.ear03.PlaylistActivity;
import theboltentertainment.ear03.R;
import theboltentertainment.ear03.Views.PlayButton;


public class PlaylistsViewAdapter extends RecyclerView.Adapter<PlaylistsViewAdapter.ViewHolder>{
    private ArrayList<Playlist> playlists;
    private Context c;

    public PlaylistsViewAdapter(Context c, ArrayList<Playlist> playlists) {
        this.playlists = playlists;
        this.c = c;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistsViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (playlists.get(position).getCover() != null) {
            Picasso.with(c).load(new File(playlists.get(position).getCover())).resize(holder.imgSize, holder.imgSize)
                    .centerInside().into(holder.cover);
        }
        holder.title.setText(playlists.get(position).getName());

        ArrayAdapter adapter = new ArrayAdapter<>(c, R.layout.textview, playlists.get(position).getDisplaySongs());
        holder.list.setAdapter(adapter);

        holder.shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.shufflePlaylist(position);
            }
        });
        holder.flow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.flowPlaylist(position);
            }
        });
        holder.cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, PlaylistActivity.class);
                i.putExtra(PlaylistActivity.PLAYLIST, MainActivity.playlists.get(position));
                c.startActivity(i);
            }
        });

        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView cover;
        private TextView title;
        private ListView list;
        private PlayButton play;
        private ImageButton shuffle;
        private ImageButton flow;
        private ImageButton cancel;

        int imgSize;
        final float playWidth;
        float flingDis, velocity;
        float playHeight;

        PlayButton.ScaleWidthAnimator collapse;
        PlayButton.ScaleWidthAnimator expand;
        FlingAnimation floatShuffle;
        FlingAnimation floatFlow;

        ViewHolder(View v) {
            super(v);
            cover   = (ImageView) v.findViewById(R.id.playlist_cover);
            title   = (TextView) v.findViewById(R.id.playlist_title);
            list    = (ListView) v.findViewById(R.id.playlist_list);
            play    = (PlayButton) v.findViewById(R.id.playlist_play);
            shuffle = (ImageButton) v.findViewById(R.id.playlist_shuffle);
            flow    = (ImageButton) v.findViewById(R.id.playlist_flow);
            cancel  = (ImageButton) v.findViewById(R.id.playlist_cancel);

            imgSize = cover.getLayoutParams().width;
            playWidth = play.getLayoutParams().width;
            flingDis = playWidth / 2;
            velocity = flingDis * 4 / 1; // px per second

            play.setPivotX(playWidth / 2);
            shuffle.setPivotX(shuffle.getLayoutParams().width/2);
            flow.setPivotX(flow.getLayoutParams().width/2);

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expandAnimation();
                }
            });
        }

        private void expandAnimation() {
            shuffle.setVisibility(View.INVISIBLE);
            flow.setVisibility(View.INVISIBLE);
            cancel.setVisibility(View.INVISIBLE);

            play.setText("");
            playHeight = ( playHeight==0 ) ? play.getMeasuredHeight() : playHeight;
            if (expand == null) {
                expand = new PlayButton.ScaleWidthAnimator(play, playHeight, playWidth, 200);

                expand.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        play.setText("Play");
                        cancel.setVisibility(View.GONE);

                        play.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                expandAnimation();
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
            if (collapse == null) {
                collapse = new PlayButton.ScaleWidthAnimator(play, playWidth, playHeight, 200);
                collapse.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        play.setOnClickListener(null);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        play.setVisibility(View.GONE);
                        cancel.setVisibility(View.VISIBLE);

                        shuffle.setVisibility(View.VISIBLE);
                        flow.setVisibility(View.VISIBLE);

                        floatFlow = new FlingAnimation(flow, DynamicAnimation.X)
                                .setMinValue(flow.getX() - flingDis).setMaxValue(flow.getX())
                                .setStartVelocity(-velocity).setFriction(1.0f).addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                                    @Override
                                    public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                                        cancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                collapseAnimation();
                                            }
                                        });
                                    }
                                });
                        floatShuffle = new FlingAnimation(shuffle, DynamicAnimation.X)
                                .setMinValue(shuffle.getX()).setMaxValue(shuffle.getX() + flingDis)
                                .setStartVelocity(velocity).setFriction(1.0f).addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                                    @Override
                                    public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                                        cancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                collapseAnimation();
                                            }
                                        });
                                    }
                                });

                        floatShuffle.start();
                        floatFlow.start();

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
            play.startAnimation(collapse);
        }

        private void collapseAnimation() {
            cancel.setOnClickListener(null);
            floatFlow = new FlingAnimation(flow, DynamicAnimation.X)
                    .setMinValue(flow.getX()).setMaxValue(flow.getX() + flingDis)
                    .setStartVelocity(velocity).setFriction(1.0f)
                    .addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                        @Override
                        public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                            flow.setVisibility(View.GONE);
                        }
                    });
            floatShuffle = new FlingAnimation(shuffle, DynamicAnimation.X)
                    .setMinValue(shuffle.getX() - flingDis).setMaxValue(shuffle.getX())
                    .setStartVelocity(-velocity).setFriction(1.0f)
                    .addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                        @Override
                        public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                            shuffle.setVisibility(View.GONE);
                        }
                    });

            new FlingAnimation(cancel, DynamicAnimation.ROTATION).setStartVelocity(-1100f)
                    .setFriction(2f)
                    .addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                        @Override
                        public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                            play.setVisibility(View.VISIBLE);
                            cancel.setVisibility(View.GONE);

                            floatFlow.start();
                            floatShuffle.start();
                            play.startAnimation(expand);
                        }
                    }).start();

        }
    }
}
