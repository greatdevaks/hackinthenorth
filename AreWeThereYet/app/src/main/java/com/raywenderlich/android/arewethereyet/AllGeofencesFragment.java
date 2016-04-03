package com.raywenderlich.android.arewethereyet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.software.shell.fab.ActionButton;

import java.util.ArrayList;
import java.util.List;

public class AllGeofencesFragment extends Fragment implements AddGeofenceFragment.AddGeofenceFragmentListener {

  // region Properties

  private ViewHolder viewHolder;

  private ViewHolder getViewHolder() {
    return viewHolder;
  }

  private AllGeofencesAdapter allGeofencesAdapter;

  // endregion

  // region Overrides

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_all_geofences, container, false);
    viewHolder = new ViewHolder();
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getViewHolder().populate(view);

    viewHolder.geofenceRecyclerView.setHasFixedSize(true);

    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    viewHolder.geofenceRecyclerView.setLayoutManager(layoutManager);

    allGeofencesAdapter = new AllGeofencesAdapter(GeofenceController.getInstance().getNamedGeofences());
    viewHolder.geofenceRecyclerView.setAdapter(allGeofencesAdapter);
    allGeofencesAdapter.setListener(new AllGeofencesAdapter.AllGeofencesAdapterListener() {
      @Override
      public void onDeleteTapped(NamedGeofence namedGeofence) {
        List<NamedGeofence> namedGeofences = new ArrayList<>();
        namedGeofences.add(namedGeofence);
        GeofenceController.getInstance().removeGeofences(namedGeofences, geofenceControllerListener);
      }
    });

    viewHolder.actionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AddGeofenceFragment dialogFragment = new AddGeofenceFragment();
        dialogFragment.setListener(AllGeofencesFragment.this);
        dialogFragment.show(getActivity().getSupportFragmentManager(), "AddGeofenceFragment");
      }
    });

    refresh();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_delete_all) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setMessage(R.string.AreYouSure)
              .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                  GeofenceController.getInstance().removeAllGeofences(geofenceControllerListener);
                }
              })
              .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                  // User cancelled the dialog
                }
              })
              .create()
              .show();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  // endregion


  // region GeofenceControllerListener

  private GeofenceController.GeofenceControllerListener geofenceControllerListener = new GeofenceController.GeofenceControllerListener() {
    @Override
    public void onGeofencesUpdated() {
      refresh();
    }

    @Override
    public void onError() {
      showErrorToast();
    }
  };

  // endregion

  // region Private

  private void refresh() {
    allGeofencesAdapter.notifyDataSetChanged();

    if (allGeofencesAdapter.getItemCount() > 0) {
      getViewHolder().emptyState.setVisibility(View.INVISIBLE);
    } else {
      getViewHolder().emptyState.setVisibility(View.VISIBLE);

    }

    getActivity().invalidateOptionsMenu();

  }

  private void showErrorToast() {
    Toast.makeText(getActivity(), getActivity().getString(R.string.Toast_Error), Toast.LENGTH_SHORT).show();
  }

  // endregion

  // region AddGeofenceFragmentListener

  @Override
  public void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog, NamedGeofence geofence) {
    GeofenceController.getInstance().addGeofence(geofence, geofenceControllerListener);
  }

  @Override
  public void onDialogNegativeClick(android.support.v4.app.DialogFragment dialog) {
    // Do nothing
  }

  // endregion

  // region Inner classes

  static class ViewHolder {
    ViewGroup container;
    ViewGroup emptyState;
    RecyclerView geofenceRecyclerView;
    ActionButton actionButton;

    public void populate(View v) {
      container = (ViewGroup) v.findViewById(R.id.fragment_all_geofences_container);
      emptyState = (ViewGroup) v.findViewById(R.id.fragment_all_geofences_emptyState);
      geofenceRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_all_geofences_geofenceRecyclerView);
      actionButton = (ActionButton) v.findViewById(R.id.fragment_all_geofences_actionButton);

      actionButton.setImageResource(R.drawable.fab_plus_icon);
    }
  }

  // endregion
}
