/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hive.ql.optimizer.calcite.cost;

import org.apache.calcite.adapter.druid.DruidQuery;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.hadoop.hive.ql.optimizer.calcite.HivePlannerContext;
import org.apache.hadoop.hive.ql.optimizer.calcite.rules.HiveDruidRules;

/**
 * Refinement of {@link org.apache.calcite.plan.volcano.VolcanoPlanner} for Hive.
 * 
 * <p>
 * It uses {@link org.apache.hadoop.hive.ql.optimizer.calcite.cost.HiveCost} as
 * its cost model.
 */
public class HiveVolcanoPlanner extends VolcanoPlanner {
  private static final boolean ENABLE_COLLATION_TRAIT = true;

  /** Creates a HiveVolcanoPlanner. */
  public HiveVolcanoPlanner(HivePlannerContext conf) {
    super(HiveCost.FACTORY, conf);
  }

  public static RelOptPlanner createPlanner(HivePlannerContext conf) {
    final VolcanoPlanner planner = new HiveVolcanoPlanner(conf);
    planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
    if (ENABLE_COLLATION_TRAIT) {
      planner.addRelTraitDef(RelCollationTraitDef.INSTANCE);
    }
    return planner;
  }

  @Override
  public void registerClass(RelNode node) {
    if (node instanceof DruidQuery) {
      // Special handling for Druid rules here as otherwise
      // planner will add Druid rules with logical builder
      addRule(HiveDruidRules.FILTER);
      addRule(HiveDruidRules.PROJECT_FILTER_TRANSPOSE);
      addRule(HiveDruidRules.AGGREGATE_FILTER_TRANSPOSE);
      addRule(HiveDruidRules.AGGREGATE_PROJECT);
      addRule(HiveDruidRules.PROJECT);
      addRule(HiveDruidRules.AGGREGATE);
      addRule(HiveDruidRules.POST_AGGREGATION_PROJECT);
      addRule(HiveDruidRules.FILTER_AGGREGATE_TRANSPOSE);
      addRule(HiveDruidRules.FILTER_PROJECT_TRANSPOSE);
      addRule(HiveDruidRules.SORT_PROJECT_TRANSPOSE);
      addRule(HiveDruidRules.SORT);
      addRule(HiveDruidRules.PROJECT_SORT_TRANSPOSE);
      return;
    }
    super.registerClass(node);
  }
}
