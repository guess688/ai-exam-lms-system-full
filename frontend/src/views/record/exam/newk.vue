<template>
  <el-container style="height: 100vh; border: 1px solid #eee">
    <!-- <div class="left">
      <div class="fk">
        <div
          style="
            font-size: 25px;
            font-weight: 500;
            margin-left: 10px;
            padding: 10px 0 0 0;
          "
        >
          11
        </div>
        <div class="sj">
          <div>
            <span>得分</span>
            <span>50</span>
          </div>
          <div>
            <span>耗时</span>
            <span>50</span>
          </div>
          <div>
            <span>提交人</span>
            <span>50</span>
          </div>
          <el-divider></el-divider>
          <p>
            共 <span style="color: #1890ff">5 </span> 题, 共
            <span style="color: #1890ff">100</span> 分
          </p>
          <el-row>
            <el-tag
              v-for="index in 5"
              :type="index === quIndex ? 'success' : ''"
              @click="handleTag(index)"
              class="type_tag"
            >
              {{ index }}
            </el-tag>
          </el-row>
        </div>
      </div>
    </div> -->

    <el-container>
      <el-main class="right">
        <div class="ai-report-actions">
          <div>
            <div class="ai-report-title">AI 学情报告</div>
            <div class="ai-report-meta">基于本次考试、章节知识点掌握、错题、网课进度和学习任务生成</div>
          </div>
          <div class="ai-report-buttons">
            <el-button
              type="primary"
              icon="el-icon-cpu"
              :loading="aiReportLoading"
              @click="handleGenerateAiReport"
            >
              生成 AI 学情报告
            </el-button>
            <el-button icon="el-icon-document" @click="openAiReportDialog">
              查看 AI 学情报告
            </el-button>
          </div>
        </div>
        <el-dialog
          title="AI 学情报告"
          :visible.sync="aiReportDialogVisible"
          width="760px"
          append-to-body
        >
          <div v-if="!aiReports.length" class="ai-report-empty">暂无 AI 学情报告</div>
          <el-collapse v-else v-model="activeReportId" accordion>
            <el-collapse-item
              v-for="report in aiReports"
              :key="report.id"
              :name="String(report.id)"
            >
              <template slot="title">
                <span class="ai-report-item-title">{{ report.title || 'AI 学情报告' }}</span>
                <span class="ai-report-item-time">{{ report.createTime }}</span>
              </template>
              <div class="ai-report-summary">{{ report.outputText || '报告已生成' }}</div>
              <pre class="ai-report-json">{{ stringifyReport(report) }}</pre>
              <div class="ai-report-dialog-actions">
                <el-button
                  size="mini"
                  type="primary"
                  plain
                  :loading="aiReportLoading"
                  @click.stop="handleRegenerateAiReport(report.id)"
                >
                  重新生成
                </el-button>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-dialog>
        <div class="chart-grid">
          <div class="chart-card">
            <div class="chart-title">个人章节掌握图</div>
            <div v-if="!chapterAnalysis.length" class="empty-chart">暂无数据</div>
            <div ref="studentChapterChart" class="chart-box" />
          </div>
          <div class="chart-card">
            <div class="chart-title">个人知识点掌握图</div>
            <div v-if="!knowledgeAnalysis.length" class="empty-chart">暂无数据</div>
            <div ref="studentKnowledgeChart" class="chart-box" />
          </div>
          <div class="chart-card">
            <div class="chart-title">错题知识点分布图</div>
            <div v-if="!wrongKnowledgeData.length" class="empty-chart">暂无数据</div>
            <div ref="studentWrongKnowledgeChart" class="chart-box" />
          </div>
          <div class="chart-card">
            <div class="chart-title">历次成绩趋势</div>
            <div v-if="!trendScoreData.length" class="empty-chart">暂无数据</div>
            <div ref="studentTrendChart" class="chart-box" />
          </div>
        </div>
        <el-tabs v-model="analysisTab" class="analysis-tabs">
          <el-tab-pane label="我的章节掌握情况" name="chapter">
            <el-table :data="chapterAnalysis" border empty-text="暂无章节掌握数据">
              <el-table-column prop="name" label="章节" min-width="160" />
              <el-table-column prop="total" label="总题数" align="center" width="100" />
              <el-table-column prop="correct" label="答对题数" align="center" width="100" />
              <el-table-column prop="rate" label="正确率" align="center" width="110">
                <template slot-scope="{ row }">{{ formatRate(row.rate) }}</template>
              </el-table-column>
              <el-table-column prop="level" label="掌握等级" align="center" width="120" />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="我的知识点掌握情况" name="knowledge">
            <el-table :data="knowledgeAnalysis" border empty-text="暂无知识点掌握数据">
              <el-table-column prop="name" label="知识点" min-width="160" />
              <el-table-column prop="total" label="总题数" align="center" width="100" />
              <el-table-column prop="correct" label="答对题数" align="center" width="100" />
              <el-table-column prop="rate" label="正确率" align="center" width="110">
                <template slot-scope="{ row }">{{ formatRate(row.rate) }}</template>
              </el-table-column>
              <el-table-column prop="level" label="掌握等级" align="center" width="120" />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="我的薄弱点" name="weak">
            <el-table :data="weakKnowledgeAnalysis" border empty-text="暂无薄弱点数据">
              <el-table-column type="index" label="排名" align="center" width="80" />
              <el-table-column prop="name" label="知识点" min-width="160" />
              <el-table-column prop="total" label="总题数" align="center" width="100" />
              <el-table-column prop="correct" label="答对题数" align="center" width="100" />
              <el-table-column prop="rate" label="正确率" align="center" width="110">
                <template slot-scope="{ row }">{{ formatRate(row.rate) }}</template>
              </el-table-column>
              <el-table-column prop="level" label="掌握等级" align="center" width="120" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
        <el-col>
          <el-card class="qu_list">
            <div>
              <!-- eslint-disable-next-line vue/no-template-shadow -->
              <template v-for="(index, indexx) in data">
                <!-- eslint-disable-next-line vue/require-v-for-key -->
                <div
                  v-if="
                    index.quType === 1 ||
                    index.quType === 2 ||
                    index.quType === 3
                  "
                  :class="'index' + index"
                >
                  <el-row :gutter="24">
                    <el-col :span="20" style="text-align: left">
                      <!-- 题目: 序号、类型、题干 -->
                      <div>
                        <!-- <div class="qu_num">{{ index }}</div> -->
                        <!-- 【 单选题 】 -->
                        <div class="qu_content">
                          {{ indexx + 1 }}、{{ index.title }}
                        </div>

                        <!-- <div v-if="item.image != null && item.image != ''" style="clear: both">
                          <el-image :src="item.image" style="max-width: 200px" />
                        </div> -->
                      </div>
                      <div v-if="index.image != null && index.image != ''">
                        <el-image :src="index.image" 
                        :preview-src="[index.image]" 
                         style="height: 100px" />
                      </div>
                      <!-- 选项 -->
                      <el-radio-group class="qu_choose_group">
                        <!-- ['A', 'B', 'C', 'D'] -->
                        <el-radio
                          v-for="(item, indexs) in index.option"
                          :key="indexs"
                          :label="item.content"
                          border
                          class="qu_choose"
                          :class="{
                            imgC: item.image != null && item.image != '',
                            isRight:
                              index.myOption != null &&
                              isCheck(index.myOption, item.sort) &&
                              item.isRight,
                            incorrect:
                              index.myOption != null &&
                              isCheck(index.myOption, item.sort) &&
                              !item.isRight,
                          }"
                        >
                          <!-- 选项flex浮动 -->
                          <div class="qu_choose_tag">
                            <div class="qu_choose_tag_type">
                              {{ numberToLetter(indexs) }}、{{ item.content }}.
                            </div>
                            <!-- 选项内容和图片 -->
                            <div
                              v-if="item.image != null && item.image != ''"
                              style="clear: both"
                            >
                              <el-image
                                :src="item.image"
                                :preview-src="[item.image]" 
                                style="max-width: 200px"
                              />
                            </div>
                            <div v-if="item.image != null && item.image != ''">
                              <el-image
                                :src="item.image"
                                :preview-src="[item.image]" 
                                class="qu_choose_tag_img"
                              />
                            </div>
                          </div>
                        </el-radio>
                      </el-radio-group>

                      <!-- 题目解析 -->
                      <div class="qu_analysis">
                        <el-card>
                          <div>
                            <span>考生答案：</span>
                            <span>{{ numberToLetter(index.myOption) }}</span
                            ><br />
                          </div>
                          <div style="margin-top: 8px">
                            <span>正确答案：</span>
                            <span>{{ numberToLetter(index.rightOption) }}</span
                            ><br />
                          </div>
                          <div style="margin-top: 8px">
                            <span>试题解析：</span>
                            <span>{{ index.analyse }}</span
                            ><br />
                          </div>
                        </el-card>
                      </div>
                    </el-col>
                  </el-row>
                  <el-divider />
                </div>
              </template>
              <!-- eslint-disable-next-line vue/no-template-shadow -->
              <template v-for="index in data">
                <!-- eslint-disable-next-line vue/require-v-for-key -->
                <div v-if="index.quType === 4" :class="'index' + index">
                  <el-row :gutter="24">
                    <el-col :span="20" style="text-align: left">
                      <!-- 题目: 序号、类型、题干 -->
                      <div>
                        <!-- <div class="qu_num">{{ index }}</div> -->
                        <!-- 【 单选题 】 -->
                        <div class="qu_content">{{ index.title }}</div>
                      </div>

                      <!-- 选项 -->
                      <el-radio-group class="qu_choose_group">
                        <!-- ['A', 'B', 'C', 'D'] -->
                        <el-input
                          v-model="index.myOption"
                          style="margin-top: 10px"
                          type="textarea"
                          :autosize="{ minRows: 2, maxRows: 4 }"
                          placeholder=""
                          :disabled="true"
                        />
                      </el-radio-group>

                      <!-- 题目解析 -->
                      <div class="qu_analysis">
                        <el-card>
                          <div>
                            <!-- <span>考生答案：</span>
                            <span
                              :style="{
                                color:
                                  isRight === 1
                                    ? 'green'
                                    : isRight === 0
                                    ? 'red'
                                    : 'gray',
                              }"
                              >{{}}</span
                            ><br /> -->
                          </div>
                          <div style="margin-top: 8px">
                            <span>正确答案：</span>
                            <span>{{ index.rightOption }}</span>
                            <br />
                          </div>
                          <div style="margin-top: 8px">
                            <span>试题解析：</span>
                            <span>{{ index.analyse }}</span
                            ><br />
                          </div>
                        </el-card>
                      </div>
                    </el-col>
                  </el-row>
                  <el-divider />
                </div>
              </template>
            </div>
            <el-divider />
          </el-card>
        </el-col>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import echarts from "echarts";
import { recordExamDetail } from "@/api/record";
import {
  getStudentChapterAnalysis,
  getStudentKnowledgeAnalysis,
  getStudentScoreTrend
} from "@/api/score";
import {
  fetchStudentAiReportHistory,
  generateStudentAiReport,
  regenerateStudentAiReport
} from "@/api/ai";
export default {
  name: "ExamProcess",
  data() {
    return {
      input: "",
      quIndex: -1,
      examId: 0,
      data: null,
      userId: null,
      analysisTab: "chapter",
      chapterAnalysis: [],
      knowledgeAnalysis: [],
      weakKnowledgeAnalysis: [],
      trendScoreData: [],
      chartInstances: {},
      aiReportLoading: false,
      aiReportDialogVisible: false,
      aiReports: [],
      activeReportId: null,
      index: {
        quType: 4, // 确保这里有一个值
      },
    };
  },
  computed: {
    wrongKnowledgeData() {
      return this.knowledgeAnalysis
        .map((item) => ({
          name: item.name,
          value: Math.max(Number(item.total || 0) - Number(item.correct || 0), 0),
        }))
        .filter((item) => item.value > 0);
    },
  },
  created() {
    if (this.$route.query?.data?.type === 1) {
      this.userId = this.$route.query.data.userId;
      localStorage.setItem("record_exam_userId", this.userId);
    } else if (localStorage.getItem("record_exam_userId")) {
      this.userId = localStorage.getItem("record_exam_userId");
    }
    // this.examId=this.$route.query.zhi.examId
    this.examId = localStorage.getItem("record_exam_examId");
    this.ExamDetail();
    this.loadMasteryAnalysis();
    this.loadAiReports(true);
  },
  mounted() {
    this.initCharts();
    window.addEventListener("resize", this.resizeCharts);
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.resizeCharts);
    this.destroyCharts();
  },
  methods: {
    isCheck(myOption, sort) {
      const arr = myOption.split(",").map(Number); // 将字符串转换为数字数组
      if (arr.includes(sort)) {
        return true;
      } else {
        return false;
      }
    },
    numberToLetter(input) {
      const numberToCharMap = {
        0: "A",
        1: "B",
        2: "C",
        3: "D",
        4: "E",
        5: "F",
      };

      // 辅助函数：将单个数字（字符串或数字类型）转换为字母
      const singleNumberToLetter = (num) =>
        numberToCharMap[parseInt(num, 10)] || "";

      // 辅助函数：处理逗号分隔的数字字符串
      const commaSeparatedNumbersToLetters = (str) => {
        const numbers = str.split(",").map((item) => parseInt(item.trim(), 10));
        return numbers.map((number) => numberToCharMap[number] || "").join(",");
      };

      // 判断输入类型并调用相应函数
      if (/^\d+$/.test(input)) {
        // 单个数字（字符串形式也可以匹配）
        return singleNumberToLetter(input);
      } else if (/^\d+(,\d+)*$/.test(input)) {
        // 包含逗号分隔的数字字符串
        return commaSeparatedNumbersToLetters(input);
      } else {
        return ""; // 输入不符合预期，返回空字符串或根据需要处理
      }
    },
    // 分页查询
    async ExamDetail() {
      const params = { examId: this.examId, userId: this.userId };
      const res = await recordExamDetail(params);
      this.data = res.data;
    },
    async loadMasteryAnalysis() {
      if (!this.examId) {
        return;
      }
      const params = { examId: this.examId };
      if (this.userId) {
        params.userId = this.userId;
      }
      const trendParams = {};
      if (this.userId) {
        trendParams.userId = this.userId;
      }
      const [chapterRes, knowledgeRes, trendRes] = await Promise.all([
        getStudentChapterAnalysis(params),
        getStudentKnowledgeAnalysis(params),
        getStudentScoreTrend(trendParams),
      ]);
      this.chapterAnalysis = chapterRes.data || [];
      this.knowledgeAnalysis = knowledgeRes.data || [];
      this.trendScoreData = trendRes.data || [];
      this.weakKnowledgeAnalysis = [...this.knowledgeAnalysis].sort((a, b) => {
        return Number(a.rate || 0) - Number(b.rate || 0);
      });
      this.$nextTick(this.updateCharts);
    },
    buildAiReportParams() {
      const params = { examId: this.examId };
      if (this.userId) {
        params.studentId = this.userId;
      }
      return params;
    },
    async loadAiReports(silent = false) {
      if (!this.examId) {
        return;
      }
      try {
        const res = await fetchStudentAiReportHistory(this.buildAiReportParams());
        this.aiReports = res.data || [];
        if (this.aiReports.length && !this.activeReportId) {
          this.activeReportId = String(this.aiReports[0].id);
        }
      } catch (error) {
        if (!silent) {
          this.$message.error("AI 学情报告加载失败");
        }
      }
    },
    async handleGenerateAiReport() {
      if (!this.examId) {
        this.$message.warning("缺少考试信息，无法生成报告");
        return;
      }
      this.aiReportLoading = true;
      try {
        const res = await generateStudentAiReport(this.buildAiReportParams());
        this.$message.success("AI 学情报告已生成");
        await this.loadAiReports(true);
        if (res.data) {
          this.activeReportId = String(res.data.id);
        }
        this.aiReportDialogVisible = true;
      } catch (error) {
        this.$message.error("AI 学情报告生成失败");
      } finally {
        this.aiReportLoading = false;
      }
    },
    async handleRegenerateAiReport(reportId) {
      if (!reportId) {
        return;
      }
      this.aiReportLoading = true;
      try {
        const res = await regenerateStudentAiReport(reportId);
        this.$message.success("AI 学情报告已重新生成");
        await this.loadAiReports(true);
        if (res.data) {
          this.activeReportId = String(res.data.id);
        }
      } catch (error) {
        this.$message.error("AI 学情报告重新生成失败");
      } finally {
        this.aiReportLoading = false;
      }
    },
    async openAiReportDialog() {
      await this.loadAiReports(false);
      this.aiReportDialogVisible = true;
    },
    stringifyReport(report) {
      const source = report.outputJson || report.outputText || "";
      if (!source) {
        return "暂无报告内容";
      }
      try {
        const parsed = typeof source === "string" ? JSON.parse(source) : source;
        return JSON.stringify(parsed, null, 2);
      } catch (error) {
        return String(source);
      }
    },
    formatRate(rate) {
      if (rate === null || rate === undefined) {
        return "0%";
      }
      return `${Number(rate).toFixed(2)}%`;
    },
    // 点击答题卡题号, 右侧题目滑动
    initCharts() {
      this.$nextTick(() => {
        [
          "studentChapterChart",
          "studentKnowledgeChart",
          "studentWrongKnowledgeChart",
          "studentTrendChart",
        ].forEach((ref) => {
          if (this.$refs[ref] && !this.chartInstances[ref]) {
            this.chartInstances[ref] = echarts.init(this.$refs[ref]);
          }
        });
        this.updateCharts();
      });
    },
    destroyCharts() {
      Object.keys(this.chartInstances).forEach((key) => {
        if (this.chartInstances[key]) {
          this.chartInstances[key].dispose();
        }
      });
      this.chartInstances = {};
    },
    resizeCharts() {
      Object.keys(this.chartInstances).forEach((key) => {
        if (this.chartInstances[key]) {
          this.chartInstances[key].resize();
        }
      });
    },
    updateCharts() {
      this.setBarChart("studentChapterChart", this.chapterAnalysis, "rate", "正确率(%)", "#E6A23C");
      this.setBarChart("studentKnowledgeChart", this.knowledgeAnalysis, "rate", "正确率(%)", "#409EFF");
      this.setBarChart("studentWrongKnowledgeChart", this.wrongKnowledgeData, "value", "错题数", "#F56C6C");
      this.setLineChart("studentTrendChart", this.trendScoreData);
    },
    setBarChart(ref, rows, valueKey, yName, color) {
      const chart = this.chartInstances[ref];
      if (!chart) {
        return;
      }
      const names = (rows || []).map((item) => this.shortLabel(item.name || "未命名"));
      const values = (rows || []).map((item) => Number(item[valueKey] || 0));
      chart.setOption({
        tooltip: { trigger: "axis" },
        grid: { left: 48, right: 20, top: 24, bottom: names.length > 6 ? 72 : 42 },
        xAxis: {
          type: "category",
          data: names,
          axisLabel: {
            interval: 0,
            rotate: names.length > 6 ? 30 : 0,
          },
        },
        yAxis: { type: "value", name: yName },
        series: [{
          type: "bar",
          data: values,
          barMaxWidth: 42,
          itemStyle: { color },
        }],
      }, true);
    },
    setLineChart(ref, rows) {
      const chart = this.chartInstances[ref];
      if (!chart) {
        return;
      }
      const names = (rows || []).map((item) => this.shortLabel(item.name || "未命名"));
      const values = (rows || []).map((item) => Number(item.value || 0));
      chart.setOption({
        tooltip: { trigger: "axis" },
        grid: { left: 48, right: 20, top: 24, bottom: names.length > 6 ? 72 : 42 },
        xAxis: {
          type: "category",
          data: names,
          axisLabel: {
            interval: 0,
            rotate: names.length > 6 ? 30 : 0,
          },
        },
        yAxis: { type: "value", name: "得分" },
        series: [{
          type: "line",
          data: values,
          smooth: true,
          symbolSize: 7,
          lineStyle: { color: "#67C23A" },
          itemStyle: { color: "#67C23A" },
        }],
      }, true);
    },
    shortLabel(value) {
      const text = String(value || "");
      return text.length > 12 ? `${text.slice(0, 12)}...` : text;
    },
    handleTag(index) {
      // 高亮选中的题目index标签
      this.quIndex = index;
      // 题目滑动到锚定点
      const page = document.querySelector(".index" + index);
      page.scrollIntoView();
    },
  },
};
</script>

<style scoped lang="scss">
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.ai-report-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  background: #fff;
}

.ai-report-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.ai-report-meta {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.ai-report-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.ai-report-empty {
  padding: 32px 0;
  text-align: center;
  color: #909399;
}

.ai-report-item-title {
  font-weight: 600;
  color: #303133;
}

.ai-report-item-time {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.ai-report-summary {
  margin-bottom: 10px;
  color: #606266;
  line-height: 1.6;
}

.ai-report-json {
  max-height: 360px;
  overflow: auto;
  padding: 12px;
  border: 1px solid #ebeef5;
  background: #f8fafc;
  color: #303133;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-report-dialog-actions {
  margin-top: 12px;
  text-align: right;
}

.chart-card {
  position: relative;
  min-height: 290px;
  padding: 14px;
  border: 1px solid #ebeef5;
  background: #fff;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.chart-box {
  width: 100%;
  height: 240px;
}

.empty-chart {
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  text-align: center;
  color: #909399;
  z-index: 1;
}

.content {
  width: 97%;
  height: 60px;
  border: 1px solid #0a84ff;
  margin-top: 8px;
  margin-left: 10px;
  padding: 10px;
  font-weight: 200;
}
.sj {
  margin-top: 10px;
  margin-left: 10px;
  line-height: 22px;
}
.isRight {
  background-color: rgb(215, 245, 215);
}
.incorrect {
  background-color: rgb(248, 197, 197);
}
.fk {
  width: 200px;
  height: 100%;
  box-shadow: 0 0 15px rgb(197, 197, 197);
  margin: auto;
  margin-top: 20px;
  margin-left: 15px;
}
.el-header {
  background-color: #b3c0d1;
  color: #333;
  line-height: 60px;
}

.left {
  width: 250px;
  height: 100%;
}
.right {
  width: 70%;
  height: 100%;
}
.analysis-tabs {
  margin-bottom: 16px;
}
.el-divider--horizontal {
  display: block;
  height: 1px;
  width: 95%;
  margin: 24px 0;
}
.type_tag {
  margin-right: 5px;
  margin-top: 10px;
}

// 试题内容样式
.qu_list {
  height: 100%;
  width: 100%;
  overflow: auto;
  page-break-after: always;

  .qu_num {
    display: inline-block;
    // background: url('~@/assets/images/tkxl/btbj.png') no-repeat 100% 100%;
    background-size: contain;
    height: 30px;
    width: 30px;
    line-height: 25px;
    color: #fff;
    font-size: 14px;
    text-align: center;
    margin-right: 15px;
    flex-shrink: 0;
  }

  .qu_content {
    padding-left: 10px;
  }

  // 选项组
  .qu_choose_group {
    width: 100%;

    // 单个选项
    .qu_choose {
      display: block;
      margin: 10px;

      // 去除前面的radio
      ::v-deep .el-radio__input .el-radio__inner {
        display: none;
      }

      // 单个选项内容样式
      .qu_choose_tag {
        display: inline-flex;
        width: 90%;
        // 选项标签
        .qu_choose_tag_type {
          font-weight: bold;
          // color: #0a84ff;
          width: 10px;
        }
        // 选项内容
        .qu_choose_tag_content {
          padding: 0 10px 10px 10px;
        }
        .qu_choose_tag_img {
          // max-height:90px;
          // max-width:300px;
          height: 100px;
          display: block;
          margin: 10px;
        }

        .qu_choose_tag_el_image {
          clear: both;
          padding-top: 10px;
        }
      }
      // 选项答案
      .qu_choose_answer {
        float: right;
      }
    }
  }

  // 试题解析
  .qu_analysis {
    padding: 10px;

    .qu_analysis_content {
      padding-top: 10px;
    }
  }

  // 试题赋分
  .qu_assign_score {
    background: #f5f5f5;
    height: 100px;
    padding-top: 35px;

    .qu_assign_score_content {
      width: 80px;
    }
  }
}
.imgC {
  height: 150px;
}
</style>
