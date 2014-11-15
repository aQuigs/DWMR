package com.cse280.dwmr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class NoteActivity extends ActionBarActivity implements ActionBar.TabListener
{
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager            mViewPager;
    ActionBar            ab;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Button add = (Button) findViewById(R.id.btAddNote);
        Button del = (Button) findViewById(R.id.btDeleteNote);

        ab = getSupportActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                ab.setSelectedNavigationItem(position);
            }
        });

        add.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addTab();
            }
        });

        del.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mSectionsPagerAdapter.count > 1)
                    deleteCurrentTab();
                else
                    Toast.makeText(NoteActivity.this, "Cannot delete last note", Toast.LENGTH_LONG).show();
            }
        });
        addTab();
    }

    private void addTab()
    {
        final ActionBar ab = getSupportActionBar();
        Tab newtab = ab.newTab().setText(mSectionsPagerAdapter.getPageTitle(mSectionsPagerAdapter.count++))
                .setTabListener(this);
        ab.addTab(newtab);
        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(newtab.getPosition());
        mViewPager.invalidate();
    }

    private void deleteCurrentTab()
    {
        final ActionBar ab = getSupportActionBar();
        ab.removeTab(ab.getSelectedTab());
        --mSectionsPagerAdapter.count;
        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        public int  count;
        private int nextName;

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
            count = 0;
            nextName = 1;
        }

        @Override
        public Fragment getItem(int position)
        {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount()
        {
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return "Note " + (nextName++);
        }
    }

    public static class PlaceholderFragment extends Fragment
    {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber)
        {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_note, container, false);
            return rootView;
        }
    }

}
