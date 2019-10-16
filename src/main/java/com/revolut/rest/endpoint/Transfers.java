package com.revolut.rest.endpoint;

import com.revolut.db.DbOperation;
import com.revolut.pojo.Message;
import lombok.extern.java.Log;
import org.jooq.Record;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.revolut.pojo.ResponseMessage.NO_RECORD;
import static org.jooq.h2.generated.Tables.TRANSFERS;

@RequestScoped
@Path("/transfers")
@Log
public class Transfers {

    @Inject
    private DbOperation dbOperation;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTransfers() {
        return Response.ok(
                dbOperation.executeAndReturn(
                        context -> context.select()
                                .from(TRANSFERS)
                                .fetch()
                                .map(
                                        record -> Json.createObjectBuilder()
                                                .add(TRANSFERS.ID.getName(), record.getValue(TRANSFERS.ID))
                                                .add(TRANSFERS.FROM_ACCOUNT.getName(), record.getValue(TRANSFERS.FROM_ACCOUNT))
                                                .add(TRANSFERS.TO_ACCOUNT.getName(), record.getValue(TRANSFERS.TO_ACCOUNT))
                                                .add(TRANSFERS.AMOUNT.getName(), record.getValue(TRANSFERS.AMOUNT))
                                                .add(TRANSFERS.AT.getName(), record.getValue(TRANSFERS.AT).toString())
                                                .add(TRANSFERS.COMMENT.getName(), record.getValue(TRANSFERS.COMMENT))
                                                .build()
                                )
                )
        ).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{transferId}")
    public Response getTransfer(@PathParam("transferId") long transferId) {
        Record record = dbOperation.executeAndReturn(
                context -> context.select()
                        .from(TRANSFERS)
                        .where(TRANSFERS.ID.equal(transferId))
                        .fetchAny()
        );
        log.info("record " + record);

        if (record == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .header("cause", NO_RECORD)
                    .entity(new Message(NO_RECORD))
                    .build();
        }

        return Response.ok(
                Json.createObjectBuilder()
                        .add(TRANSFERS.ID.getName(), record.getValue(TRANSFERS.ID))
                        .add(TRANSFERS.FROM_ACCOUNT.getName(), record.getValue(TRANSFERS.FROM_ACCOUNT))
                        .add(TRANSFERS.TO_ACCOUNT.getName(), record.getValue(TRANSFERS.TO_ACCOUNT))
                        .add(TRANSFERS.AMOUNT.getName(), record.getValue(TRANSFERS.AMOUNT))
                        .add(TRANSFERS.AT.getName(), record.getValue(TRANSFERS.AT).toString())
                        .add(TRANSFERS.COMMENT.getName(), record.getValue(TRANSFERS.COMMENT))
                        .build()
        ).build();
    }
}