package com.example.prarthana.entertainmentapp;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import java.util.*;

public class SectionPageAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList=new ArrayList<>();
        private final List<String> mFragmentTitleList=new ArrayList<>();
        private Context context;
        private int[] icon={
                R.drawable.search_icon,
                R.drawable.fav_icon
        };
        public void addFragment(Fragment fragment,String title)
        {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public SectionPageAdapter(FragmentManager fm,Context context){
            super(fm);
            this.context=context;
        }

        @Override
        public CharSequence getPageTitle(int position){
            Drawable image = context.getResources().getDrawable(icon[position],null);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            // Replace blank spaces with image icon
            SpannableString sb = new SpannableString("   " + mFragmentTitleList.get(position));
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }
        @Override
        public Fragment getItem(int position){
            return mFragmentList.get(position);
        }
        @Override
        public int getCount(){
            return mFragmentList.size();
    }
}
