/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.tools.hbase.phoenix.config;

import static com.wl4g.infra.common.lang.DateUtils2.formatDate;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;
import static org.apache.commons.lang3.time.DateUtils.addDays;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import com.wl4g.infra.common.io.FileIOUtils;
import com.wl4g.tools.hbase.phoenix.fake.AbstractFaker.FakeProvider;
import com.wl4g.tools.hbase.phoenix.util.RowKeySpec;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link PhoenixFakeProperties}
 * 
 * @author James Wong
 * @version 2022-10-22
 * @since v1.0.0
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PhoenixFakeProperties implements InitializingBean {

    private File metaCsvFile = new File(USER_HOME + "/.phoenix-fake-tool/meta.csv");

    private String tableNamespace = "safeclound";

    private String tableName = "tb_ammeter";

    private boolean dryRun = true;

    private int threadPools = 1;

    private int maxLimit = 1440 * 10;

    private boolean errorContinue = false;

    private long awaitSeconds = Duration.ofMillis(30).getSeconds();

    private RowKeySpec rowKey = new RowKeySpec();

    private SampleConfig sample = new SampleConfig();

    private GeneratorConfig generator = new GeneratorConfig();

    private SimpleColumnFakerConfig simpleFaker = new SimpleColumnFakerConfig();

    private CumulativeColumnFakerConfig cumulativeFaker = new CumulativeColumnFakerConfig();

    private FakeProvider provider = FakeProvider.CUMULATIVE;

    @Override
    public void afterPropertiesSet() throws Exception {
        FileIOUtils.forceMkdirParent(metaCsvFile);
        // Ensure initialized.
        rowKey.ensureInit();
    }

    /**
     * 从历史数据采样配置
     */
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class SampleConfig {
        private String startDate = formatDate(addDays(new Date(), -2), "yyyyMMddHHmm");
        private String endDate = formatDate(addDays(new Date(), -1), "yyyyMMddHHmm");
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class GeneratorConfig {
        /**
         * 时间刻度: </br>
         * 1. 用于采样时获取过去一段"时间量"的历史数据; </br>
         * 2. 用于生成 fake 数据 rowKey 时使用(样本时间+此时间刻度的量)
         */
        private String rowKeyDatePattern = "dd";
        private int rowKeyDateAmount = 1;
        // 注: 当使用 Cumulative Fake(即递增) 模拟数据时, 最小和最大随机百分比应该 >1
        // 反之, 如果生成模拟数据无需递增, 则最小随机百分比可以 <1
        private double valueRandomMinPercent = 1.0124;
        private double valueRandomMaxPercent = 1.0987;

        // 更简单实现: lastMaxFakeValue * fakeAmount, 无需重试生成.
        // /**
        // * 随机生成满足要求的 fakeValue 值时的最大尝试次数
        // */
        // private int maxAttempts = 10;
        //
        // /**
        // * 最大努力尝试依然无法满足, 则使用: (value + fallbackFakeAmountValue)
        // */
        // private double fallbackFakeAmountValue = 31 * Math.E * Math.PI;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class SimpleColumnFakerConfig {
        private List<String> columnNames = new ArrayList<String>() {
            private static final long serialVersionUID = 1L;
            {
                // e.g: 有功功率, 无功功率
                add("activePower");
                add("reactivePower");
            }
        };
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class CumulativeColumnFakerConfig {
        /**
         * 时间量, 如: 基于过去一段时长(7d)的平均值, 使用那种"时间刻度"参考:
         * {@link GeneratorConfig#rowKeyDatePattern}
         */
        private int offsetLastDateAmount = -7;
        private List<String> columnNames = new ArrayList<String>() {
            private static final long serialVersionUID = 1L;
            {
                // e.g: 有功电度, 无功电度
                add("activePower");
                add("reactivePower");
            }
        };
    }

}
