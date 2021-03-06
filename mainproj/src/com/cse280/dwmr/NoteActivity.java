package com.cse280.dwmr;

import java.util.ArrayList;

import android.content.SharedPreferences;
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
        
        setTitle("Enter some notes");

        Button add = (Button) findViewById(R.id.btAddNote);
        Button sav = (Button) findViewById(R.id.btSaveNotes);
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
                addTab(1);
            }
        });

        sav.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                saveNotes();
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
                {
                    Toast.makeText(NoteActivity.this, "Cannot delete last note", Toast.LENGTH_LONG).show();
                    frags.get(0).getEditText().setText("");
                }
            }
        });

        // addTab(1);
        loadAndPopulateNotes();
    }

    private void loadAndPopulateNotes()
    {
        SharedPreferences sp = getSharedPreferences(Constants.NOTE_PREF, MODE_PRIVATE);
        int num_notes = sp.getInt(Constants.NUM_NOTES_KEY, 1);

        addTab(num_notes);

        for (int i = 0; i < num_notes; ++i)
        {
            String note_text = sp.getString(Constants.NOTE_CONTENT_PREFIX + i, "");
            NoteFragment nf = (NoteFragment) mSectionsPagerAdapter.getItem(i);
            Bundle textBundle = new Bundle();
            textBundle.putString(Constants.DEFAULT_TEXT, note_text);
            nf.setArguments(textBundle);
        }
    }

    private void saveNotes()
    {
        ArrayList<String> notes = getNoteTexts(null);
        SharedPreferences.Editor sp = getSharedPreferences(Constants.NOTE_PREF, MODE_PRIVATE).edit();
        sp.clear();
        sp.putInt(Constants.NUM_NOTES_KEY, notes.size());

        for (int i = 0; i < notes.size(); ++i)
        {
            sp.putString(Constants.NOTE_CONTENT_PREFIX + i, notes.get(i));
        }

        Toast.makeText(NoteActivity.this, "Notes Saved", Toast.LENGTH_SHORT).show();
        sp.commit();
        finish();
    }

    private void addTab(int amount)
    {
        Tab newtab = null;
        for (int i = 0; i < amount; ++i)
        {
            newtab = ab.newTab().setText(mSectionsPagerAdapter.getPageTitle(mSectionsPagerAdapter.count++))
                    .setTabListener(this);
            ab.addTab(newtab);
        }

        // to handle cache issues leading to nullptr exceptions
        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.count);
        if (newtab != null)
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
            View v = getView();
            return (v == null) ? null : (EditText) v.findViewById(R.id.etNote1);
        }

        public NoteFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_note, container, false);

            Bundle args = getArguments();
            if (args != null)
            {
                String def = args.getString(Constants.DEFAULT_TEXT);
                ((EditText) rootView.findViewById(R.id.etNote1)).setText((def == null) ? "" : def);
            }

            return rootView;
        }
    }
}
