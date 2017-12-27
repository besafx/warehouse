package com.besafx.app.rest;

import com.besafx.app.config.CustomException;
import com.besafx.app.entity.BillBuy;
import com.besafx.app.entity.Person;
import com.besafx.app.search.BillBuySearch;
import com.besafx.app.service.BillBuyService;
import com.besafx.app.service.PersonService;
import com.besafx.app.ws.Notification;
import com.besafx.app.ws.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;

@RestController
@RequestMapping(value = "/api/billBuy/")
public class BillBuyRest {

    private final static Logger log = LoggerFactory.getLogger(BillBuyRest.class);

    private final String FILTER_TABLE = "**,billBuyType[**,-lastPerson],branch[id,code,name],lastPerson[id,contact[id,firstName,forthName]]";

    @Autowired
    private PersonService personService;

    @Autowired
    private BillBuyService billBuyService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private BillBuySearch billBuySearch;

    @RequestMapping(value = "create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_BILL_BUY_CREATE')")
    public String create(@RequestBody BillBuy billBuy, Principal principal) {
        if(billBuyService.findByCode(billBuy.getCode()) != null){
            throw new CustomException("رقم الفاتورة مسجل سابقاً");
        }
        Person person = personService.findByEmail(principal.getName());
        billBuy.setLastUpdate(new Date());
        billBuy.setLastPerson(person);
        billBuy = billBuyService.save(billBuy);
        notificationService.notifyAll(Notification.builder().message("تم انشاء فاتورة جديدة بنجاح").type("success").build());
        return SquigglyUtils.stringify(Squiggly.init(new ObjectMapper(), FILTER_TABLE), billBuy);
    }

    @RequestMapping(value = "update", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_BILL_BUY_UPDATE')")
    public String update(@RequestBody BillBuy billBuy, Principal principal) {
        Person person = personService.findByEmail(principal.getName());
        BillBuy object = billBuyService.findOne(billBuy.getId());
        if (object != null) {
            billBuy.setLastUpdate(new Date());
            billBuy.setLastPerson(person);
            billBuy = billBuyService.save(billBuy);
            notificationService.notifyAll(Notification.builder().message("تم تعديل بيانات الفاتورة بنجاح").type("warning").build());
            return SquigglyUtils.stringify(Squiggly.init(new ObjectMapper(), FILTER_TABLE), billBuy);
        } else {
            return null;
        }
    }

    @RequestMapping(value = "delete/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_BILL_BUY_DELETE')")
    public void delete(@PathVariable Long id, Principal principal) {
        BillBuy object = billBuyService.findOne(id);
        if (object != null) {
            billBuyService.delete(id);
            notificationService.notifyAll(Notification.builder().message("تم حذف الفاتورة بنجاح").type("error").build());
        }
    }

    @RequestMapping(value = "findAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String findAll() {
        return SquigglyUtils.stringify(Squiggly.init(new ObjectMapper(), FILTER_TABLE), Lists.newArrayList(billBuyService.findAll()));
    }

    @RequestMapping(value = "findOne/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String findOne(@PathVariable Long id) {
        return SquigglyUtils.stringify(Squiggly.init(new ObjectMapper(), FILTER_TABLE), billBuyService.findOne(id));
    }

    @RequestMapping(value = "filter", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String filter(
            @RequestParam(value = "codeFrom", required = false) final Long codeFrom,
            @RequestParam(value = "codeTo", required = false) final Long codeTo,
            @RequestParam(value = "dateFrom", required = false) final Long dateFrom,
            @RequestParam(value = "dateTo", required = false) final Long dateTo,
            @RequestParam(value = "amountFrom", required = false) final Long amountFrom,
            @RequestParam(value = "amountTo", required = false) final Long amountTo,
            @RequestParam(value = "branchId", required = false) final Long branchId) {
        return SquigglyUtils.stringify(Squiggly.init(new ObjectMapper(), FILTER_TABLE), billBuySearch.search(codeFrom, codeTo, dateFrom, dateTo, amountFrom, amountTo, branchId));
    }

}
