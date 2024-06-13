package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * InfoItemsCollector.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <http://www.gnu.org/licenses/>.
 */

public abstract class InfoItemsCollector<I extends InfoItem, E extends InfoItemExtractor>
        implements Collector<I, E> {

    private final List<I> itemList = new ArrayList<>();
    private final List<Throwable> errors = new ArrayList<>();
    private final int serviceId;
    @Nullable
    private final Comparator<I> comparator;

    /**
     * Create a new collector with no comparator / sorting function
     * @param serviceId the service id
     */
    public InfoItemsCollector(final int serviceId) {
        this(serviceId, null);
    }

    /**
     * Create a new collector
     * @param serviceId the service id
     */
    public InfoItemsCollector(final int serviceId, @Nullable final Comparator<I> comparator) {
        this.serviceId = serviceId;
        this.comparator = comparator;
    }

    @Override
    public List<I> getItems() {
        if (comparator != null) {
            itemList.sort(comparator);
        }
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
    protected void addError(final Exception error) {
        errors.add(error);
    }

    /**
     * Add an item
     * @param item the item
     */
    protected void addItem(final I item) {
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
    public void commit(final E extractor) {
        try {
            addItem(extract(extractor));
        } catch (final FoundAdException ae) {
            // found an ad. Maybe a debug line could be placed here
        } catch (final ParsingException e) {
            addError(e);
        }
    }
}
