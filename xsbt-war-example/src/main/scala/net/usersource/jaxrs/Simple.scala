
package net.usersource.jaxrs

import javax.ws.rs.{GET, Path, Produces}
import javax.ws.rs.core.{Response, MediaType}


@Path("/simple")
class Simple {

  @GET
  @Produces(Array{MediaType.TEXT_PLAIN})
  def get: Response = {
    Response.ok("its works").build
  }

}

