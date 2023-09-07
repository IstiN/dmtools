package com.github.istin.dmtools.report.freemarker;

import com.github.istin.dmtools.common.model.IBlocker;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

public class TicketLinkCell extends GenericCell {

    public TicketLinkCell(String ticketKey, String ticketLink) {
        super(url(ticketKey, ticketLink));
    }

    public TicketLinkCell(Collection<? extends IBlocker> tickets, Function<IBlocker, String> function) throws IOException {
        super(urls(tickets, function));
    }

    private static String urls(Collection<? extends IBlocker> tickets, Function<IBlocker, String> function) throws IOException {
        StringBuilder buffer = new StringBuilder();
        for (IBlocker blocker : tickets) {
            if (buffer.length() > 0) {
                buffer.append(",");
            }
            buffer.append(url(blocker.getTicketKey(), blocker.getTicketLink())).append("[").append(blocker.getStatus()).append("]").append(function.apply(blocker));
        }
        return buffer.toString();
    }

    @NotNull
    private static String url(String ticketKey, String ticketLink) {
        return "<a href=\"" + ticketLink + "\">" + ticketKey + "</a>";
    }
}
