/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.model;

import org.springframework.data.domain.Page;

/**
 * Details about pagination. It is zero-based, but you can use the *Ui properties to get the same values in one-based.
 *
 * <pre>
 * Dependencies:
 * compile 'org.apache.commons:commons-lang3:3.6'
 * compile 'com.fasterxml.jackson.core:jackson-databind:2.9.1'
 * compile 'org.slf4j:slf4j-api:1.7.25'
 * compile 'org.springframework.data:spring-data-jpa:1.11.7.RELEASE' (optional if using the constructor with the {@link Page}.
 * </pre>
 */
public class ApiPagination extends AbstractApiBase {

    private long currentPage;
    private long totalPages;
    private long itemsPerPage;
    private long totalItems;

    public ApiPagination() {
    }

    public ApiPagination(long currentPage, long totalPages, long itemsPerPage, long totalItems) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.itemsPerPage = itemsPerPage;
        this.totalItems = totalItems;
    }

    public ApiPagination(Page<?> page) {
        this.currentPage = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.setItemsPerPage(page.getSize());
        this.totalItems = page.getTotalElements();
    }

    /**
     * Current page.
     *
     * @return 0 to (getTotalPages - 1)
     */
    public long getCurrentPage() {
        return currentPage;
    }

    /**
     * Current page.
     *
     * @return 1 to (getTotalPages)
     */
    public long getCurrentPageUi() {
        return currentPage + 1;
    }

    public long getItemsPerPage() {
        return itemsPerPage;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public boolean isFirstPage() {
        return currentPage == 0;
    }

    public boolean isLastPage() {
        return currentPage == totalPages - 1;
    }

    /**
     * Set the current page.
     *
     * @param currentPage
     *            0 to (getTotalPages - 1)
     * @return this
     */
    public ApiPagination setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
        return this;
    }

    /**
     * Set the current page.
     *
     * @param currentPageUi
     *            1 to (getTotalPages)
     * @return this
     */
    public ApiPagination setCurrentPageUi(long currentPageUi) {
        this.currentPage = currentPageUi - 1;
        return this;
    }

    public ApiPagination setItemsPerPage(long itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        return this;
    }

    public ApiPagination setTotalItems(long totalItems) {
        this.totalItems = totalItems;
        return this;
    }

    public ApiPagination setTotalPages(long totalPages) {
        this.totalPages = totalPages;
        if (this.totalPages == 0) {
            this.totalPages = 1;
        }
        return this;
    }

}
