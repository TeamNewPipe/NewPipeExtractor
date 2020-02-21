package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * InfoItemsCollector.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public abstract class InfoItemsCollector<I extends InfoItem, E extends InfoItemExtractor> implements Collector<I, E> {

    private final List<I> itemList = new ArrayList<>();
    private final List<Throwable> errors = new ArrayList<>();
    private final int serviceId;

    /**
     * Create a new collector
     * @param serviceId the service id
     */
    public InfoItemsCollector(int serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public List<I> getItems() {
        return Collections.unmodifiableList(itemList);
    }

    @Override
    public List<Throwable> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    @Override
    public void reset() {
        itemList.clear();
        errors.clear();
    }

    /**
     * Add an error
     * @param error the error
     */
    protected void addError(Exception error) {
        errors.add(error);
    }

    /**
     * Add an item
     * @param item the item
     */
    protected void addItem(I item) {
        itemList.add(item);
    }

    /**
     * Get the service id
     * @return the service id
     */
    public int getServiceId() {
        return serviceId;
    }

    @Override
    public void commit(E extractor) {
        try {
            addItem(extract(extractor));
        } catch (FoundAdException ae) {
            // found an ad. Maybe a debug line could be placed here
        } catch (ParsingException e) {
            addError(e);
        }
    }
}
