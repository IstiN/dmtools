package com.github.istin.dmtools.common.timeline;

import com.github.istin.dmtools.common.utils.DateUtils;
import com.github.istin.dmtools.report.IReleaseGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class WeeksReleaseGenerator implements IReleaseGenerator {

    private final String startDateAsString;
    private final Calendar startDate = Calendar.getInstance();
    private List<Release> releases = new ArrayList<>();

    /**
     *
     * @param startDateAsString "dd.MM.yyyy"
     * @throws IOException connection errors
     */
    public WeeksReleaseGenerator(String startDateAsString) throws IOException {
        this.startDateAsString = startDateAsString;
        Calendar startDate = DateUtils.parseCalendar(startDateAsString); // Example start date (you can replace this with actual start date parsing)
        startDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        this.startDate.setTime(startDate.getTime());
        Calendar now = Calendar.getInstance();
        int counter = 0;


        while (startDate.before(now)) {
            Release release = new Release();
            release.setId(counter);
            release.setName("Week " + counter);
            int finalCounter = counter;
            Sprint sprint = new Sprint(finalCounter, startDate.getTime(), getOneWeekLater(startDate).getTime(), -1) {
                @Override
                public String getIterationName() {
                    return "Week " + finalCounter;
                }
            };

            boolean matchedToSprintTimelines = sprint.isMatchedToSprintTimelines(now);
            sprint.setIsCurrent(matchedToSprintTimelines);
            release.setSprints(Collections.singletonList(sprint));
            releases.add(release);
            counter++;

            // Increment startDate by one week
            startDate.add(Calendar.WEEK_OF_YEAR, 1);
        }

        // releases now contains weekly releases until the current week
        for (Release release : releases) {
            System.out.println("Release ID: " + release.getId() + ", Name: " + release.getName());
        }
    }

    // Helper method to get date one week later
    private static Calendar getOneWeekLater(Calendar date) {
        Calendar oneWeekLater = (Calendar) date.clone();
        oneWeekLater.add(Calendar.WEEK_OF_YEAR, 1);
        return oneWeekLater;
    }

    public List<Release> getReleases() {
        return releases;
    }

    @Override
    public List<Release> generate() {
        return releases;
    }

    @Override
    public int getTypeOfReleases() {
        return Release.Style.BY_RELEASE.ordinal();
    }

    @Override
    public Release getCurrentIteration() {
        for (Release release : releases) {
            if (release.getIsCurrent()) {
                return release;
            }
        }
        return null;
    }

    @Override
    public int getStartSprint() {
        return 0;
    }

    @Override
    public int getStartFixVersion() {
        return 0;
    }

    @Override
    public int getExtraSprintTimeline() {
        return 0;
    }

    @Override
    public Calendar getStartDate() {
        return startDate;
    }

    @Override
    public long getMaxTime() {
        return 0;
    }
}
