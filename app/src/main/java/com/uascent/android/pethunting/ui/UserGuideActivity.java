package com.uascent.android.pethunting.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.uascent.android.pethunting.R;
import com.uascent.android.pethunting.adapter.UserGuideFragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/4/13.
 */
public class UserGuideActivity extends BaseActivity implements View.OnClickListener,
        BaseFragment.BuntonClickListener {
//    private ImageView ivIndicatorFirst;
//    private ImageView ivIndicatorSecond;
//    private ImageView ivIndicatorThird;
//    private ImageView ivIndicatorFouth;
//    private ImageView ivIndicatorFiveth;
//
//    private Button btPlay;

    private ViewPager viewPager;
    private ArrayList<Fragment> mFragmentList;
    private int fragmentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_userguide);
        super.onCreate(savedInstanceState);
        setUI();
        initViewPage();
    }

    private void setUI() {
//        btPlay = (Button) findViewById(R.id.btnStart);
//        btPlay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(UserGuideActivity.this, ConnectCatActivity.class);
//                UserGuideActivity.this.startActivity(intent);
//                finish();
//            }
//        });
//        ivIndicatorFirst = (ImageView) findViewById(R.id.ivIndicatorFirst);
//        ivIndicatorFirst.setOnClickListener(this);
//        ivIndicatorSecond = (ImageView) findViewById(R.id.ivIndicatorSecond);
//        ivIndicatorSecond.setOnClickListener(this);
//        ivIndicatorThird = (ImageView) findViewById(R.id.ivIndicatorThird);
//        ivIndicatorThird.setOnClickListener(this);
//        ivIndicatorFouth = (ImageView) findViewById(R.id.ivIndicatorFouth);
//        ivIndicatorFouth.setOnClickListener(this);
//        ivIndicatorFiveth = (ImageView) findViewById(R.id.ivIndicatorFiveth);
//        ivIndicatorFiveth.setOnClickListener(this);
        viewPager = (ViewPager) findViewById(R.id.vpMain);
        BaseFragment.setBuntonClickListener(this);

    }

    private void initViewPage() {
        UserGuide1Fragment one = new UserGuide1Fragment();
        UserGuide2Fragment two = new UserGuide2Fragment();
        UserGuide3Fragment three = new UserGuide3Fragment();
        UserGuide4Fragment four = new UserGuide4Fragment();
        UserGuide5Fragment five = new UserGuide5Fragment();
        mFragmentList = new ArrayList<Fragment>();
        mFragmentList.add(one);
        mFragmentList.add(two);
        mFragmentList.add(three);
        mFragmentList.add(four);
        mFragmentList.add(five);

        //ViewPager set adapter
        viewPager.setAdapter(new UserGuideFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList));
        //ViewPager page change listener
//        viewPager.setOnPageChangeListener(new mOnPageChangeListener());
        //ViewPager show first fragment
        changeFragment(0);
    }

    private void changeFragment(int item) {
        viewPager.setCurrentItem(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.ivIndicatorFirst:
//                fragmentIndex = 0;
//                break;
//            case R.id.ivIndicatorSecond:
//                fragmentIndex = 1;
//                break;
//            case R.id.ivIndicatorThird:
//                fragmentIndex = 2;
//                break;
//            case R.id.ivIndicatorFouth:
//                fragmentIndex = 3;
//                break;
//            case R.id.ivIndicatorFiveth:
//                fragmentIndex = 4;
//                break;
            default:
                break;
        }
//        changeFragment(fragmentIndex);

    }

    @Override
    public void onButtonClick(int index) {
        if (index != 6) {
            changeFragment(index);
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    /**
     * ViewPager change Fragment, Text Color change
     */
//    private class mOnPageChangeListener implements ViewPager.OnPageChangeListener {
//
//        @Override
//        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//        }
//
//        @Override
//        public void onPageSelected(int position) {
//            switch (position) {
//                case 0:
//                    ivIndicatorFirst.setImageResource(R.drawable.page_indicator_focused);
//                    ivIndicatorSecond.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorThird.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorFouth.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorFiveth.setImageResource(R.drawable.page_indicator);
//                    btPlay.setVisibility(View.GONE);
//                    break;
//                case 1:
//                    ivIndicatorFirst.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorSecond.setImageResource(R.drawable.page_indicator_focused);
//                    ivIndicatorThird.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorFouth.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorFiveth.setImageResource(R.drawable.page_indicator);
//                    btPlay.setVisibility(View.GONE);
//                    break;
//                case 2:
//                    ivIndicatorFirst.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorSecond.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorThird.setImageResource(R.drawable.page_indicator_focused);
//                    ivIndicatorFouth.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorFiveth.setImageResource(R.drawable.page_indicator);
//                    btPlay.setVisibility(View.GONE);
//                    break;
//                case 3:
//                    ivIndicatorFirst.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorSecond.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorThird.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorFouth.setImageResource(R.drawable.page_indicator_focused);
//                    ivIndicatorFiveth.setImageResource(R.drawable.page_indicator);
//                    btPlay.setVisibility(View.GONE);
//                    break;
//                case 4:
//                    ivIndicatorFirst.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorSecond.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorThird.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorFouth.setImageResource(R.drawable.page_indicator);
//                    ivIndicatorFiveth.setImageResource(R.drawable.page_indicator_focused);
//                    btPlay.setVisibility(View.VISIBLE);
//                    break;
//            }
//        }
//
//        @Override
//        public void onPageScrollStateChanged(int state) {
//
//        }
//    }


}
