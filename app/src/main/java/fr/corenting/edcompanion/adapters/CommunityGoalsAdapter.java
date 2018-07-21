package fr.corenting.edcompanion.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.listener.ITableViewListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.corenting.edcompanion.R;
import fr.corenting.edcompanion.activities.DetailsActivity;
import fr.corenting.edcompanion.models.CommunityGoal;
import fr.corenting.edcompanion.models.CommunityGoalReward;
import fr.corenting.edcompanion.utils.MiscUtils;

public class CommunityGoalsAdapter extends RecyclerView.Adapter<CommunityGoalsAdapter.goalsViewHolder> {

    private List<CommunityGoal> goals;
    private View.OnClickListener onClickListener;

    private Context context;
    private boolean isDetailsView;

    public CommunityGoalsAdapter(final Context context, final RecyclerView recyclerView, final List<CommunityGoal> goals, boolean isDetailsView) {
        this.context = context;
        this.isDetailsView = isDetailsView;
        this.goals = goals;

        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDetailsView(recyclerView.getChildAdapterPosition(v));
            }
        };
    }

    @Override
    public goalsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        if (!isDetailsView) {
            v.setOnClickListener(onClickListener);
        }
        return new goalsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final goalsViewHolder holder, final int position) {
        CommunityGoal currentGoal = goals.get(position);

        // Content
        holder.titleTextView.setText(currentGoal.getTitle());
        holder.objectiveTextView.setText(currentGoal.getObjective());
        holder.subtitleTextView.setText(currentGoal.getRefreshDateString(context));
        holder.peopleTextView.setText(String.valueOf(currentGoal.getContributors()));
        holder.remainingTextView.setText(currentGoal.getEndDate(context));
        holder.tierTextView.setText(currentGoal.getTierString());
        holder.locationTextView.setText(currentGoal.getSystem());

        // Description
        if (isDetailsView) {
            holder.descriptionTextView.setMaxLines(Integer.MAX_VALUE);
        }
        holder.descriptionTextView.setText(currentGoal.getDescription());

        // Rewards table
        if (currentGoal.getRewards() != null && currentGoal.getRewards().size() != 0) {

            // Setup view
            holder.rewardsTableView.setVisibility(View.VISIBLE);
            holder.rewardsTableView.setRowHeaderWidth(0);
            holder.rewardsTableView.setEnabled(false);
            holder.rewardsTableView
                    .setSelectedColor(context.getResources().getColor(android.R.color.transparent));
            setTableClickListeners(position, holder.rewardsTableView);

            // Adapter
            CommunityGoalRewardAdapter adapter = new CommunityGoalRewardAdapter(context);
            holder.rewardsTableView.setAdapter(adapter);

            // Set item lists
            List<String> columnHeadersList = new ArrayList<>();
            columnHeadersList.add(context.getString(R.string.tier));
            columnHeadersList.add(context.getString(R.string.contributions));
            columnHeadersList.add(context.getString(R.string.reward));
            List<String> rowHeadersList = new ArrayList<>();
            List<List<String>> cellList = new ArrayList<>();
            for (CommunityGoalReward goalReward : currentGoal.getRewards()) {
                List<String> item = new ArrayList<>();
                item.add(goalReward.getTier());
                item.add(goalReward.getContributors());
                item.add(goalReward.getRewards());
                cellList.add(item);
            }
            adapter.setAllItems(columnHeadersList, rowHeadersList, cellList);
        }
        else {
            holder.rewardsTableView.setVisibility(View.GONE);
        }

        // Set click listeners
        setClickListeners(position, holder.peopleTextView, R.string.hint_participants);
        setClickListeners(position, holder.remainingTextView, R.string.hint_end_date);
        setClickListeners(position, holder.tierTextView, R.string.hint_tiers);
        setClickListeners(position, holder.locationTextView, R.string.hint_system);
        setClickListeners(position, holder.descriptionTextView, R.string.community_goal_description);
        setClickListeners(position, holder.objectiveTextView, R.string.objective);
    }

    private void setClickListeners(final int position, final TextView textView,
                                   final int labelResId) {
        // Set long click listener for copying informations to clipboard
        textView.setOnLongClickListener(null);
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                MiscUtils.putTextInClipboardWithNotification(context,
                        context.getString(labelResId),
                        textView.getText().toString());
                return true;
            }
        });

        // Setup a regular click listener for details view
        textView.setOnClickListener(null);
        if (!isDetailsView) {
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchToDetailsView(position);
                }
            });
        }
    }

    private void setTableClickListeners(final int position, final TableView tableView) {
        tableView.setTableViewListener(null);
        tableView.setTableViewListener(new ITableViewListener() {
            @Override
            public void onCellClicked(@NonNull RecyclerView.ViewHolder cellView, int column, int row) {
                tableView.getSelectionHandler().clearSelection();
            }

            @Override
            public void onCellLongPressed(@NonNull RecyclerView.ViewHolder cellView, int column, int row) {
                tableView.getSelectionHandler().clearSelection();
                CommunityGoalRewardAdapter.CellViewHolder cellViewHolder =
                        (CommunityGoalRewardAdapter.CellViewHolder) cellView;
                MiscUtils.putTextInClipboardWithNotification(context,
                        "",
                        cellViewHolder.cellTextView.getText().toString());
            }

            @Override
            public void onColumnHeaderClicked(@NonNull RecyclerView.ViewHolder columnHeaderView, int column) {
                tableView.getSelectionHandler().clearSelection();
            }

            @Override
            public void onColumnHeaderLongPressed(@NonNull RecyclerView.ViewHolder columnHeaderView, int column) {
                tableView.getSelectionHandler().clearSelection();
            }

            @Override
            public void onRowHeaderClicked(@NonNull RecyclerView.ViewHolder rowHeaderView, int row) {
                tableView.getSelectionHandler().clearSelection();
            }

            @Override
            public void onRowHeaderLongPressed(@NonNull RecyclerView.ViewHolder rowHeaderView, int row) {
                tableView.getSelectionHandler().clearSelection();
            }
        });
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    private void switchToDetailsView(int position) {
        final CommunityGoal goal = goals.get(position);
        Intent i = new Intent(context, DetailsActivity.class);
        i.putExtra("goal", goal);
        context.startActivity(i);
    }

    public static class goalsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.titleTextView) TextView titleTextView;
        @BindView(R.id.objectiveTextView) TextView objectiveTextView;
        @BindView(R.id.subtitleTextView) TextView subtitleTextView;
        @BindView(R.id.descriptionTextView) TextView descriptionTextView;
        @BindView(R.id.remainingTextView) TextView remainingTextView;
        @BindView(R.id.tierTextView) TextView tierTextView;
        @BindView(R.id.peopleTextView) TextView peopleTextView;
        @BindView(R.id.locationTextView) TextView locationTextView;
        @BindView(R.id.rewardsTableView) TableView rewardsTableView;


        goalsViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            objectiveTextView.setVisibility(View.VISIBLE);
        }
    }
}
