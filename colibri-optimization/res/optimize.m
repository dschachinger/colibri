function [schedule] = optimize (path)
% This is the main optimization function.

% **************************************************************************************************
% * Copyright (c) 2016, Automation Systems Group, Institute of Computer Aided Automation, TU Wien
% * All rights reserved.
% * 
% * Redistribution and use in source and binary forms, with or without
% * modification, are permitted provided that the following conditions
% * are met:
% * 1. Redistributions of source code must retain the above copyright
% *    notice, this list of conditions and the following disclaimer.
% * 2. Redistributions in binary form must reproduce the above copyright
% *    notice, this list of conditions and the following disclaimer in the
% *    documentation and/or other materials provided with the distribution.
% * 3. Neither the name of the Institute nor the names of its contributors
% *    may be used to endorse or promote products derived from this software
% *    without specific prior written permission.
% * 
% * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
% * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
% * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
% * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
% * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
% * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
% * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
% * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
% * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
% * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
% * SUCH DAMAGE.
% *************************************************************************************************/

    % log message
    disp('MATLAB: optimize start');

    % ----------------------------------------------------------------
    
    global data;
    global constraints;
    global keys;
    global values;
    
    % ----------------------------------------------------------------
    
    % initialize variables
    keys = [];
    values = [];
    data = struct([]);
    input = struct([]);
    models = struct([]);
    constraints = struct([]);
    
    % ----------------------------------------------------------------

    % load saved values
    load(strcat(path,'/data.mat'),'data');
    load(strcat(path,'/input.mat'),'input');
    load(strcat(path,'/models.mat'),'models');
    load(strcat(path,'/constraints.mat'),'constraints');

    % ----------------------------------------------------------------
    
    % set parameters
    data.priority = input.future_priority;
    data.prices = input.future_prices;

    % create input matrix
    data.input = combine( ...
        input.current_start, ...
        data.step_interval, ...
		(data.input_occupancy - data.input_time), ...
        data.future_steps, ...
        ones((data.input_influences - data.input_occupancy),data.future_steps), ...
        input.future_influences, ...
        NaN((data.input_end - data.input_states + 1),data.future_steps), ...
        data.history_steps, ...
        input.history_occupancy, ...
        input.history_influences, ...
        input.history_states);
    
    % get data for optimization run
    [desired_comfort, ~, production, ~, occupancy, occupancy_binary] = predict('static', {}, data, input, models);
    
    % set data
    data.input(data.input_occupancy:data.input_influences-1,data.history_steps+1:end) = occupancy;    
    data.occupancy = occupancy_binary;    
    data.desired = desired_comfort;    
    constraints.nonlinear_production_b = production;
    
    % ----------------------------------------------------------------
    
    % reshape initial schedule
    if ~isempty(input.initial_schedule)      
        [x,y] = size(input.initial_schedule);
        input.initial_schedule = reshape(input.initial_schedule,[1,x*y]);
    end;
    
    % ----------------------------------------------------------------
    
    % run global optimization
    if data.global_optimization == true
        
        % set function handles
        objective_function = @objective;
        constraint_function = @constrain;
        
        % define options
        options = optimoptions('ga','PlotFcn',{@gaplotbestf,@gaplotstopping}, 'Display','iter');
        
        options = optimoptions(options,'CrossoverFcn',@(parents,options,nvars,FitnessFcn,unused,thisPopulation)crossover(parents,options,nvars,FitnessFcn,unused,thisPopulation,constraint_function));
        options = optimoptions(options,'MutationFcn', @(parents, options, nvars,FitnessFcn, state, thisScore, thisPopulation)mutate(parents, options, nvars,FitnessFcn, state, thisScore, thisPopulation,constraint_function));
        options = optimoptions(options,'SelectionFcn', @select);
        options = optimoptions(options,'CreationFcn', @(GenomeLength, FitnessFcn, options)create(GenomeLength, FitnessFcn, options, constraint_function));
        options = optimoptions(options,'PopulationSize',double(5));%data.population_size)); 
        options = optimoptions(options,'UseParallel', true);
        
        options.InitialPopulationMatrix = input.initial_schedule;
        
		% TODO test other options
		
        % 'ConstraintTolerance',0.000000000001
        % 'HybridFcn',{@fmincon, fminuncOptions}
        % 'MaxGenerations',300
        % 'FunctionTolerance',1e-10
        % 'MaxStallGenerations',50
        % 'Generations',nrgenerations,
        % 'MigrationFraction',0.2,
        % 'EliteCount',floor(0.03*data.population_size),
        % 'CrossoverFraction', 0.5
        
        % run genetic algorithm
        [x,fval] = ga(objective_function,double(constraints.number_vars),...
            constraints.linear_A,constraints.linear_b,...
            constraints.linear_Aeq,constraints.linear_beq,...
            double(constraints.lower_bounds),double(constraints.upper_bounds), ...
            constraint_function,options);
        
        % log message
        fprintf('MATLAB: optimize (global result, fval = %d)\n',fval);
        
    elseif ~isempty(input.initial_schedule)
        
        % set initial schedule as starting point for local optimization
        x = input.initial_schedule(1,:);
		
    else
        
        % set empty initial solution
        x = zeros(1,constraints.number_vars);
        
        x(1:8:end) = 100;
        x(3:8:end) = 50;
        x(5:8:end) = 20;
		
    end;
    
    % run local search to improve solution
    [x,fval] = localsearch(x);
    
    % log message
    fprintf('MATLAB: optimize (local result, fval = %d)\n',fval);

    % ----------------------------------------------------------------
    
    % restructure schedule    
    schedule = reshape(x,[size(x,2)/data.future_steps,data.future_steps]);

    % create timestamps for schedule
    time = input.current_start;
    
    times = zeros(1,data.future_steps);    
    for ts=1:data.future_steps
        times(1,ts) = posixtime(time);        
        time = time + minutes(data.step_interval);
    end;
        
    % combine schedule and times
    schedule = [times ; schedule];
    
    % ----------------------------------------------------------------
    
    % log message
    disp('MATLAB: optimize end');

    % ****************************************************************

    % This is the nested fitness function for the optimization algorithm.
    function fitness = objective(x)
                
        % no log message
        
        % ------------------------------------------------------------
        
        % get index of solution
        index = 0;
        if ~isempty(keys)
            [~, index] = ismember(x,keys,'rows');
        end;
        
        % are the consequences already available
        if index > 0
            fprintf('MATLAB: objective (solution consequences are found)\n');
            
            % load predicted values
            v = values(index).v;
            d = values(index).d;
            
        else
            
            % throw exception
            msgID = 'objective:BadIndex';
            msg = 'Unable to find solution consequences.';
            baseException = MException(msgID,msg);
            throw(baseException);
        end;
        
        % ------------------------------------------------------------
        
        % split solution
        [q, ~, s] = split(x, data);
        
        % initialize fitness
        fitness = 0;
        
        % calculate fitness value
        for t=1:data.future_steps
            
            % calculate comfort fitness
            c = 0;
            
            for p=1:data.parameters
                for z=1:data.zones
                    if data.monitor_indicator(z,p) == 1
                        c = c + (data.priority(p,t) * data.occupancy(z, t) * ((v(z,p,t) - data.desired(z,p,t))^2));
                    end;
                end;
            end;
            
            % calculate energy efficiency fitness
            e = 0;
            
            for g=1:data.grids
                for y2=1:data.suppliers
                    if data.supply_indicator(g,y2) == 1
                        e = e + (q(y2,t) * data.prices(y2,t) * s(y2,t) * d(g, t));
                    end;
                end;
            end;
            
            % TODO scale/norm comfort and energy part of the fitness
            
            % sum up fitness
            fitness = fitness + data.objective_weight * c + (1 - data.objective_weight) * e;
        end;
        
        % ------------------------------------------------------------
        
        % no log message        
        
    end

    % ****************************************************************
    
    % This nested function evaluates the non-linear constraints of a solution and runs predition models.
    function [c,ceq] = constrain(x)
        
        % no log message
        
        % ------------------------------------------------------------
        
        % split solution
        [q, l, s] = split(x, data);
        
        % get index of solution
        index = 0;
        if ~isempty(keys)
            [~, index] = ismember(x,keys,'rows');
        end;
        
        % are the consequences already available
        if index > 0
            fprintf('MATLAB: constrain (solution consequences are already available)\n');
            
            % load predicted values
            v = values(index).v;
            d = values(index).d;
            
        else
            fprintf('MATLAB: constrain (solution consequences will be predicted)\n');
            
            % store keys
            keys = [ keys ; x];
            
            % run prediction models
            [~, v, ~, d, ~, ~] = predict('nonstatic', l, data, input, models);
            
            % store predicted values
            temp.v = v;
            temp.d = d;
            values = [ values; temp ];
        end;
        
        % ------------------------------------------------------------
        
        % initialize constraint counters
        c_count = 1;
        ceq_count = 1;
        
        % calculate size of constraint arrays
        c_size = data.future_steps * ( ...
            sum(constraints.nonlinear_comfort_op(:)==1) + ...
            sum(constraints.nonlinear_comfort_op(:)==3) + ...
            sum(double(data.supply_production) .* double(sum(data.supply_indicator,1))));
        ceq_size = data.future_steps * sum(constraints.nonlinear_comfort_op(:)==2);
        
        % c <= 0
        c = zeros(1,c_size);
        
        % ceq = 0
        ceq = zeros(1,ceq_size);
        
        % ------------------------------------------------------------
        
        % define constraints for comfort conditions (threshold values)
        for i2=1:data.zones
            for j2=1:data.parameters
                
                % there is a threshold condition for zone and parameter
                if constraints.nonlinear_comfort_op(i2,j2) > 0
                    
                    % at each time step this condition needs to hold
                    for t2=1:data.future_steps
                        
                        % evaluate operator of condition
                        switch constraints.nonlinear_comfort_op(i2,j2)
                            
                            % value <= threshold
                            case 1
                                c(c_count) = v(i2,j2,t2)-constraints.nonlinear_comfort_b(i2,j2);
                                c_count = c_count + 1;
                                
                                
                                % value == threshold
                            case 2
                                ceq(ceq_count) = v(i2,j2,t2)-constraints.nonlinear_comfort_b(i2,j2);
                                ceq_count = ceq_count + 1;
                                
                                % value >= threshold
                            case 3
                                c(c_count) = -v(i2,j2,t2)+constraints.nonlinear_comfort_b(i2,j2);
                                c_count = c_count + 1;
                        end;
                    end;
                end;
            end;
        end;
        
        % ------------------------------------------------------------
        
        % define constraints for production >= demand per supplier
        for i2 = 1:data.suppliers
            
            % supplier is local producer
            if data.supply_production(1,i2) == 1
                
                % provided energy types of the supplier
                supplied_grids = data.supply_indicator(:,i2);
                
                for g=1:size(supplied_grids,1)
                    
                    % grid is supported by supplier
                    if supplied_grids(g) == 1
                        
                        % set constraints for all time slots
                        c(c_count:c_count+data.future_steps-1) = q(i2,:) .* d(g,:) .* s(i2,:) - constraints.nonlinear_production_b(i2,:);
                        c_count = c_count + data.future_steps;
                    end;
                end;
            end;
        end;
        
        % ------------------------------------------------------------
        
        % no log message
        
    end

    % ****************************************************************
    
    % This is the mutation operator for the genetic algorithm.
    function [x,fval] = localsearch(globalsolution)

        % log message
        disp('MATLAB: localsearch start');
        
        % ------------------------------------------------------------
                
        x = globalsolution;
        constrain(x);
        
        % basic principle is variable neighborhood  descent (VND)
        neighborhood = 1;
        globalFitness = objective(x);
        globalBest = x;
        
        while neighborhood <= 2
            
            % find an x' with f(x') <= f(x''), Vx'' € N(x)
            fitness = Inf;
            
            % TODO next improvement does not work as expected
            % neighborhood needs to be searched more systematically
            while fitness >= globalFitness
                
                % get solution
                temp = move(globalBest, neighborhood, 1e-3, @constrain);  
                
                % get fitness
                fitness = objective(temp);
                
            end;
                        
            if fitness < globalFitness
                globalFitness = fitness;
                globalBest = temp;
                neighborhood = 1;
            else 
                neighborhood = neighborhood + 1;
            end;            
        end;
        
        x = globalBest;
        fval = globalFitness;
        
        % ------------------------------------------------------------
        
        % log message
        disp('MATLAB: localsearch start');
        
    end
    
    % ****************************************************************
        
end
