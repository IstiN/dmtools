package com.github.istin.dmtools.ai;

import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TicketContextTest {

    private TrackerClient<ITicket> trackerClientMock;
    private ITicket ticketMock;
    private TicketContext ticketContext;

    @Before
    public void setUp() {
        trackerClientMock = Mockito.mock(TrackerClient.class);
        ticketMock = Mockito.mock(ITicket.class);
        ticketContext = new TicketContext(trackerClientMock, ticketMock);
    }

    @Test
    public void testGetOnTicketDetailsRequest() {
        TicketContext.OnTicketDetailsRequest requestMock = Mockito.mock(TicketContext.OnTicketDetailsRequest.class);
        ticketContext.setOnTicketDetailsRequest(requestMock);
        assertEquals(requestMock, ticketContext.getOnTicketDetailsRequest());
    }


    @Test
    public void testToText() throws IOException {
        when(ticketMock.toText()).thenReturn("Ticket Text");
        ITicket extraTicketMock = Mockito.mock(ITicket.class);
        when(extraTicketMock.toText()).thenReturn("Extra Ticket Text");

        ticketContext.setExtraTickets(Arrays.asList(extraTicketMock));

        String expectedText = "Ticket Text\nExtra Ticket Text";
        assertEquals(expectedText, ticketContext.toText());
    }

    @Test
    public void testSetExtraTickets() {
        ITicket extraTicketMock = Mockito.mock(ITicket.class);
        List<ITicket> extraTickets = Arrays.asList(extraTicketMock);

        ticketContext.setExtraTickets(extraTickets);
        assertEquals(extraTickets, ticketContext.getExtraTickets());
    }
}