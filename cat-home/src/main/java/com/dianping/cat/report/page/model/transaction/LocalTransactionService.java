package com.dianping.cat.report.page.model.transaction;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.report.ReportBucket;
import com.dianping.cat.storage.report.ReportBucketManager;

public class LocalTransactionService extends BaseLocalModelService<TransactionReport> {
	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalTransactionService() {
		super(TransactionAnalyzer.ID);
	}

	@Override
	protected TransactionReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		TransactionReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
			
			if (report == null) {
				report = new TransactionReport(domain);
				report.setStartTime(new Date(startTime));
				report.setEndTime(new Date(startTime + TimeHelper.ONE_HOUR - 1));
			}
		}
		return report;
	}

	private TransactionReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, TransactionAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}
}
