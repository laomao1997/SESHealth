package five.seshealthpatient.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import five.seshealthpatient.Fragments.DataPacketFragment;
import five.seshealthpatient.Fragments.DoctorInformationFragment;
import five.seshealthpatient.Fragments.HeartRateFragment;
import five.seshealthpatient.Fragments.MapFragment;
import five.seshealthpatient.Fragments.PatientInformationFragment;
import five.seshealthpatient.Fragments.RecordVideoFragment;
import five.seshealthpatient.Fragments.SendFileFragment;
import five.seshealthpatient.R;

public class DoctorActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    /**
     * A reference to the toolbar
     */
    private Toolbar toolbar;

    /**
     * Helps to manage the fragment that is being used in the main view.
     */
    private FragmentManager fragmentManager;

    /**
     * TAG to use
     */
    private static String TAG = "DoctorActivity";

    /**
     * I am using this enum to know which is the current fragment being displayed, you will see
     * what I mean with this later in this code.
     */
    private enum MenuStates {
        DOCTOR_INFO, VIEW_DATA_PACKET, HEARTRATE, RECORD_VIDEO, SEND_FILE, NAVIGATION_MAP
    }

    /**
     * The current fragment being displayed.
     */
    private DoctorActivity.MenuStates currentState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        // the default fragment on display is the doctor information
        currentState = MenuStates.DOCTOR_INFO;

        // go look for the main drawer layout
        mDrawerLayout = findViewById(R.id.doctor_drawer_layout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Set up the menu button
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        // Setup the navigation drawer, most of this code was taken from:
        // https://developer.android.com/training/implementing-navigation/nav-drawer
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Using a switch to see which item on the menu was clicked
                        switch (menuItem.getItemId()) {
                            // You can find these id's at: res -> menu -> drawer_view.xml
                            case R.id.nav_doctor_info:
                                // If the user clicked on a different item than the current item
                                if (currentState != MenuStates.DOCTOR_INFO) {
                                    // change the fragment to the new fragment
                                    ChangeFragment(new DoctorInformationFragment());
                                    currentState = MenuStates.DOCTOR_INFO;
                                }
                                break;
                            case R.id.nav_view_data_packet:
                                if (currentState != MenuStates.VIEW_DATA_PACKET) {
                                    ChangeFragment(new DataPacketFragment());
                                    currentState = MenuStates.VIEW_DATA_PACKET;
                                }
                                break;
                            case R.id.nav_signout:
                                FirebaseAuth.getInstance().signOut();
                                finish();
                                signOut();
                        }
                        return true;
                    }
                });

        // If you need to listen to specific events from the drawer layout.
        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );


        // More on this code, check the tutorial at http://www.vogella.com/tutorials/AndroidFragments/article.html
        fragmentManager = getFragmentManager();

        // Add the default Fragment once the user logged in
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.fragment_container, new DoctorInformationFragment());
        ft.commit();
    }

    /**
     * Called when one of the items in the toolbar was clicked, in this case, the menu button.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function changes the title of the fragment.
     *
     * @param newTitle The new title to write in the
     */
    public void ChangeTitle(String newTitle) {
        toolbar.setTitle(newTitle);
    }


    /**
     * This function allows to change the content of the Fragment holder
     * @param fragment The fragment to be displayed
     */
    private void ChangeFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    // Start login activity once signed out
    private void signOut(){
        Intent intent = new Intent(DoctorActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
