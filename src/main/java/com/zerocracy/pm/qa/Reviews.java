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
package com.zerocracy.pm.qa;

import com.jcabi.xml.XML;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.pm.ClaimOut;
import java.io.IOException;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * QA reviews.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Reviews {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Reviews(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Reviews bootstrap() throws IOException {
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).bootstrap("pm/qa/reviews");
        }
        return this;
    }

    /**
     * Add new review.
     * @param job The job to add
     * @param inspector The inspector
     * @param performer The performer
     * @param cash How much to pay
     * @param minutes Minutes to give
     * @param bonus Quality bonus
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public void add(final String job, final String inspector,
        final String performer, final Cash cash, final int minutes,
        final Cash bonus) throws IOException {
        if (this.exists(job)) {
            throw new SoftException(
                new Par("QA review for %s already exists").say(job)
            );
        }
        try (final Item reviews = this.item()) {
            new Xocument(reviews.path()).modify(
                new Directives()
                    .xpath("/reviews")
                    .add("review")
                    .attr("job", job)
                    .add("requested").set(new DateAsText().asString()).up()
                    .add("inspector").set(inspector).up()
                    .add("performer").set(performer).up()
                    .add("cash").set(cash).up()
                    .add("minutes").set(minutes).up()
                    .add("bonus").set(bonus).up()
            );
        }
    }

    /**
     * Remove review from the list and prepare the payment.
     * @param job The job to remove
     * @param claim The claim
     * @param good Is quality good and we should pay the bonus?
     * @return New claim
     * @throws IOException If fails
     */
    public ClaimOut remove(final String job, final boolean good,
        final ClaimOut claim) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par("There is no QA review for %s").say(job)
            );
        }
        final XML review;
        try (final Item reviews = this.item()) {
            review = new Xocument(reviews.path()).nodes(
                String.format("//review[@job= '%s']", job)
            ).get(0);
            new Xocument(reviews.path()).modify(
                new Directives().xpath(
                    String.format("//review[@job='%s']", job)
                ).strict(1).remove()
            );
        }
        final Cash bonus = new Cash.S(review.xpath("bonus/text()").get(0));
        Cash cash = new Cash.S(review.xpath("cash/text()").get(0));
        if (good) {
            cash = cash.add(bonus);
        }
        return claim
            .param("login", review.xpath("performer/text()").get(0))
            .param("cash", cash)
            .param("minutes", review.xpath("minutes/text()").get(0));
    }

    /**
     * This job exists in reviews?
     * @param job The job to check
     * @return TRUE if it exists
     * @throws IOException If fails
     */
    public boolean exists(final String job) throws IOException {
        try (final Item reviews = this.item()) {
            return !new Xocument(reviews.path()).nodes(
                String.format("//review[@job='%s']", job)
            ).isEmpty();
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("reviews.xml");
    }

}
