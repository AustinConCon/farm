/**
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.tk.profile;

import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.tk.RqUser;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Response;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rq.form.RqFormSmart;

/**
 * Kyc explicit identification.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.20
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkKyc implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkKyc(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final String user = new RqUser(this.farm, req).value();
        if (!"yegor256".equals(user)) {
            throw new RsForward(
                new RsFlash(
                    "You are not allowed to do this, sorry",
                    Level.WARNING
                )
            );
        }
        final String details = new RqFormSmart(req).single("details");
        final String login = new RqSecureLogin(new Pmo(this.farm), req).value();
        new People(this.farm).bootstrap().details(user, details);
        new ClaimOut()
            .type("User identified")
            .param("login", user)
            .param("details", details)
            .param("system", "manual")
            .author(login)
            .postTo(new Pmo(this.farm));
        new ClaimOut().type("Notify user").token("user;yegor256").param(
            "message", new Par(
                "We just identified @%s as \"%s\" manually"
            ).say(user, details)
        ).postTo(new Pmo(this.farm));
        new ClaimOut()
            .type("Notify user")
            .token(String.format("user;%s", login))
            .param(
                "message",
                new Par("We just identified you as `%s`").say(details)
            )
            .postTo(new Pmo(this.farm));
        return new RsForward(
            new RsFlash(
                new Par(
                    "@%s have been successfully identified as `%s`"
                ).say(user, details)
            ),
            String.format("/u/%s", user)
        );
    }

}
