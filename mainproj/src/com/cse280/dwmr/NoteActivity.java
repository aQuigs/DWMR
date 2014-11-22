package com.cse280.dwmr;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NoteActivity extends ActionBarActivity implements ActionBar.TabListener
{
    SectionsPagerAdapter    mSectionsPagerAdapter;
    ViewPager               mViewPager;
    ActionBar               ab;
    ArrayList<NoteFragment> frags = new ArrayList<NoteFragment>();

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
        int index = mViewPager.getCurrentItem();
        getSupportActionBar().removeTab(ab.getSelectedTab());
        frags.remove(index);
        --mSectionsPagerAdapter.count;
        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.invalidate();
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

    private ArrayList<String> getNoteTexts(Integer skip)
    {
        ArrayList<String> notes = new ArrayList<String>(frags.size());
        for (int i = 0; i < frags.size(); ++i)
        {
            if (skip == null || skip.intValue() != i)
            {
                notes.add(frags.get(i).getEditText().getText().toString());
            }
        }

        return notes;
    }

    private void restoreNoteTexts(ArrayList<String> notes)
    {
        for (int i = 0; i < notes.size(); ++i)
        {
            frags.get(i).getEditText().setText(notes.get(i));
        }

        for (int i = notes.size(); i < frags.size(); ++i)
        {
            frags.get(i).getEditText().setText("");
        }
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
            if (position < frags.size())
                return frags.get(position);

            NoteFragment fragment = new NoteFragment();
            frags.add(fragment);
            return fragment;
        }

        @Override
        public long getItemId(int position)
        {
            return getItem(position).hashCode();
        }

        @Override
        public int getItemPosition(Object object)
        {
            NoteFragment nf = (NoteFragment) object;
            for (int i = 0; i < frags.size(); i++)
            {
                if (frags.get(i) == nf)
                {
                    return i;
                }
            }

            return POSITION_NONE;
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

    public class NoteFragment extends Fragment
    {
        public EditText getEditText()
        {
            return (EditText) getView().findViewById(R.id.etNote1);
        }

        public NoteFragment()
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
