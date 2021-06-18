package org.proterra.danp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/simulation")
public class SimulationResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String start() {
        return "";
    }
}