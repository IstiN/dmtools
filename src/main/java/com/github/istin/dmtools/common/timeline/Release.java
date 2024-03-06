package com.github.istin.dmtools.common.timeline;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Release {

    public enum Style {
        BY_RELEASE,
        BY_SPRINTS,
        BY_WEEKS
    }

    private int id;

    private String name;

    private List<Sprint> sprints;

    public Release(int id, String name, List<Sprint> sprints) {
        this.id = id;
        this.name = name;
        this.sprints = sprints;
    }

    public Release() {
    }

    public List<Sprint> getSprints() {
        return sprints;
    }

    public List<? extends ReportIteration> getIterationsByStyle(Style style) {
        if (style == Style.BY_SPRINTS || style == Style.BY_RELEASE) {
            return sprints;
        }
        if (style == Style.BY_WEEKS) {
            List<ReportIteration> weeks = new ArrayList<>();
            for (Sprint sprint : getSprints()) {
                weeks.addAll(sprint.getWeeks());
            }
            return weeks;
        }
        throw new IllegalArgumentException("invalid argument");
    }

    public void setSprints(List<Sprint> sprints) {
        this.sprints = sprints;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getStartDate() {
        Date startDate = sprints.get(0).getStartDate();
        Calendar instance = Calendar.getInstance();
        instance.setTime(startDate);
        return instance;
    }

    public Calendar getEndDate() {
        Date endDate = sprints.get(sprints.size()-1).getEndDate();
        Calendar instance = Calendar.getInstance();
        instance.setTime(endDate);
        return instance;
    }

    public String getStartDateAsString() {
        return sprints.get(0).getStartDateAsString();
    }

    public String getEndDateAsString() {
        return sprints.get(sprints.size()-1).getEndDateAsString();
    }

    public boolean isMatchedToReleaseTimelines(Calendar date) {
        return getStartDate().getTimeInMillis() <= date.getTimeInMillis() && getEndDate().getTimeInMillis() >= date.getTimeInMillis();
    }

    public boolean beforeReleaseStart(Calendar date) {
        return getStartDate().getTimeInMillis() > date.getTimeInMillis();
    }

    public boolean afterReleaseEnds(Calendar date) {
        return getEndDate().getTimeInMillis() < date.getTimeInMillis();
    }

    public boolean getIsCurrent() {
        Calendar now = Calendar.getInstance();
        return isMatchedToReleaseTimelines(now);
    }

}
