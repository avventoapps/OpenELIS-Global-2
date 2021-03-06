package org.openelisglobal.testconfiguration.service;

import java.util.List;
import java.util.Locale;

import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.localization.service.LocalizationService;
import org.openelisglobal.localization.valueholder.Localization;
import org.openelisglobal.panelitem.service.PanelItemService;
import org.openelisglobal.panelitem.valueholder.PanelItem;
import org.openelisglobal.resultlimit.service.ResultLimitService;
import org.openelisglobal.resultlimits.valueholder.ResultLimit;
import org.openelisglobal.test.service.TestService;
import org.openelisglobal.test.valueholder.Test;
import org.openelisglobal.testconfiguration.controller.TestModifyEntryController.TestAddParams;
import org.openelisglobal.testconfiguration.controller.TestModifyEntryController.TestSet;
import org.openelisglobal.testresult.service.TestResultService;
import org.openelisglobal.testresult.valueholder.TestResult;
import org.openelisglobal.typeofsample.service.TypeOfSampleTestService;
import org.openelisglobal.typeofsample.valueholder.TypeOfSampleTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestModifyServiceImpl implements TestModifyService {

    @Autowired
    private TypeOfSampleTestService typeOfSampleTestService;
    @Autowired
    private PanelItemService panelItemService;
    @Autowired
    private TestService testService;
    @Autowired
    private ResultLimitService resultLimitService;
    @Autowired
    private TestResultService testResultService;
    @Autowired
    private LocalizationService localizationService;

    @Override
    public void updateTestSets(List<TestSet> testSets, TestAddParams testAddParams, Localization nameLocalization,
            Localization reportingNameLocalization, String currentUserId) {
        List<TypeOfSampleTest> typeOfSampleTest = typeOfSampleTestService
                .getTypeOfSampleTestsForTest(testAddParams.testId);
        String[] typeOfSamplesTestIDs = new String[typeOfSampleTest.size()];
        for (int i = 0; i < typeOfSampleTest.size(); i++) {
            typeOfSamplesTestIDs[i] = typeOfSampleTest.get(i).getId();
            typeOfSampleTestService.delete(typeOfSamplesTestIDs[i], currentUserId);
        }

        List<PanelItem> panelItems = panelItemService.getPanelItemByTestId(testAddParams.testId);
        for (PanelItem item : panelItems) {
            item.setSysUserId(currentUserId);
        }
        panelItemService.deleteAll(panelItems);

        List<ResultLimit> resultLimitItems = resultLimitService.getAllResultLimitsForTest(testAddParams.testId);
        for (ResultLimit item : resultLimitItems) {
            item.setSysUserId(currentUserId);
            resultLimitService.delete(item);
        }
        // resultLimitService.delete(resultLimitItems);

        for (TestSet set : testSets) {
            set.test.setSysUserId(currentUserId);
            set.test.setLocalizedTestName(nameLocalization);
            set.test.setLocalizedReportingName(reportingNameLocalization);

            // gnr: based on testAddUpdate,
            // added existing testId to process in createTestSets using
            // testAddParams.testId, delete then insert to modify for most elements

            for (Test test : set.sortedTests) {
                test.setSysUserId(currentUserId);
                // if (!test.getId().equals( set.test.getId() )) {
                testService.update(test);
                // }
            }

            updateTestNames(testAddParams.testId, nameLocalization, reportingNameLocalization, currentUserId);
            updateTestEntities(testAddParams.testId, testAddParams.loinc, currentUserId);

            set.sampleTypeTest.setSysUserId(currentUserId);
            set.sampleTypeTest.setTestId(set.test.getId());
            typeOfSampleTestService.insert(set.sampleTypeTest);

            for (PanelItem item : set.panelItems) {
                item.setSysUserId(currentUserId);
                Test nonTransiantTest = testService.getTestById(set.test.getId());
                item.setTest(nonTransiantTest);
                panelItemService.insert(item);
            }

            for (TestResult testResult : set.testResults) {
                testResult.setSysUserId(currentUserId);
                Test nonTransiantTest = testService.getTestById(set.test.getId());
                testResult.setTest(nonTransiantTest);
                testResultService.insert(testResult);
            }

            for (ResultLimit resultLimit : set.resultLimits) {
                resultLimit.setSysUserId(currentUserId);
                resultLimit.setTestId(set.test.getId());
                resultLimitService.insert(resultLimit);
            }
        }
    }

    private void updateTestEntities(String testId, String loinc, String userId) {
        Test test = testService.get(testId);

        if (test != null) {
            test.setSysUserId(userId);
            test.setLoinc(loinc);
            testService.update(test);
        }
    }

    private void updateTestNames(String testId, Localization nameLocalization, Localization reportingNameLocalization,
            String userId) {
        Test test = testService.get(testId);

        if (test != null) {
            Localization name = test.getLocalizedTestName();
            Localization reportingName = test.getLocalizedReportingName();
            for (Locale locale : localizationService.getAllActiveLocales()) {
                name.setLocalizedValue(locale, nameLocalization.getLocalizedValue(locale).trim());
                reportingName.setLocalizedValue(locale, reportingNameLocalization.getLocalizedValue(locale).trim());
            }
            name.setSysUserId(userId);
            reportingName.setSysUserId(userId);

            localizationService.update(name);
            localizationService.update(reportingName);

        }

        // Refresh test names
        DisplayListService.getInstance().getFreshList(DisplayListService.ListType.ALL_TESTS);
        DisplayListService.getInstance().getFreshList(DisplayListService.ListType.ORDERABLE_TESTS);
    }

}
