// Helpers para trabalhar com datas no fuso local (evita problemas com toISOString()/UTC)
function getLocalDateString(date = new Date()) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function parseLocalDate(dateStr) {
    // dateStr no formato YYYY-MM-DD
    if (!dateStr) return new Date();
    const parts = dateStr.split('-').map(Number);
    return new Date(parts[0], parts[1] - 1, parts[2]);
}

// Estado do usuário
let estadoUsuario = {
    dataAtual: getLocalDateString(),
    diaAtual: null,
    progresso: {},
    streak: 0,
    diasProdutivos: 0,
    inicioSistema: getLocalDateString(),
    configuracaoInicial: localStorage.getItem('configuracaoInicial') || null,
    jaConfigurado: localStorage.getItem('configuracaoJaRealizada') === 'true',
    recompensasLiberadas: JSON.parse(localStorage.getItem('recompensasLiberadas') || '{}')
};

// Inicialização
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM carregado - Inicializando sistema Usuário...');
    inicializarSistemaUsuario();
});

function inicializarSistemaUsuario() {
    console.log('Inicializando página do Usuário...');
    carregarEstadoUsuario();
    
    // VERIFICAÇÃO: Só pergunta se NUNCA configurou antes
    if (!estadoUsuario.jaConfigurado) {
        mostrarConfiguracaoInicial();
        return;
    }
    
    // IMPORTANTE: Resetar hábitos ao iniciar (garante que comecem desmarcados)
    resetarHabitosNovoDia();
    
    determinarDiaAtual();
    configurarEventListenersUsuario();
    carregarRotinaDoDia();
    calcularStreak();
    atualizarProgressoSemanal();
    atualizarContadores();
}

// NOVA FUNÇÃO: Resetar hábitos ao virar o dia
function resetarHabitosNovoDia() {
    const hoje = getLocalDateString();
    
    // Verificar se é um novo dia
    if (!estadoUsuario.progresso[hoje]) {
        estadoUsuario.progresso[hoje] = {
            dia: estadoUsuario.diaAtual,
            concluido: false,
            habitos: {}
        };
        
        // IMPORTANTE: Não copiar hábitos concluídos do dia anterior
        // Os hábitos começam sempre desmarcados em um novo dia
        
        console.log('Novo dia detectado - Hábitos resetados');
        localStorage.setItem('estadoUsuario', JSON.stringify(estadoUsuario));
    } else {
        // Se já existe progresso para hoje, garantir que não está herdando dados antigos
        const progressoHoje = estadoUsuario.progresso[hoje];
        
        // Verificar se os dados são de hoje mesmo (não de um dia anterior)
        const dataProgresso = Object.keys(estadoUsuario.progresso).find(data => data === hoje);
        if (dataProgresso && progressoHoje.habitos) {
            // Manter apenas os hábitos que foram realmente marcados hoje
            // Não fazer nada - deixar como está (usuário já começou a marcar hoje)
            console.log('Progresso de hoje já existe - mantendo estado atual');
        }
    }
}

function mostrarConfiguracaoInicial() {
    const container = document.getElementById('conteudo-rotina');
    container.innerHTML = `
        <div class="configuracao-inicial">
            <h2>🎯 Primeira Vez Aqui?</h2>
            <p>Para começar, selecione qual dia da semana é <strong>hoje</strong>:</p>
            <div class="opcoes-treino">
                <button class="btn-treino-inicial" data-dia="segunda">📅 Segunda-feira</button>
                <button class="btn-treino-inicial" data-dia="terca">📅 Terça-feira</button>
                <button class="btn-treino-inicial" data-dia="quarta">📅 Quarta-feira</button>
                <button class="btn-treino-inicial" data-dia="quinta">📅 Quinta-feira</button>
                <button class="btn-treino-inicial" data-dia="sexta">📅 Sexta-feira</button>
                <button class="btn-treino-inicial" data-dia="sabado">📅 Sábado</button>
                <button class="btn-treino-inicial" data-dia="domingo">📅 Domingo</button>
            </div>
            <p class="info-configuracao">⚠️ Esta pergunta aparecerá apenas esta vez</p>
        </div>
    `;
    
    document.querySelectorAll('.btn-treino-inicial').forEach(btn => {
        btn.addEventListener('click', function() {
            const diaInicial = this.getAttribute('data-dia');
            finalizarConfiguracaoInicial(diaInicial);
        });
    });
}

function finalizarConfiguracaoInicial(diaInicial) {
    estadoUsuario.configuracaoInicial = diaInicial;
    estadoUsuario.diaAtual = diaInicial;
    estadoUsuario.jaConfigurado = true;
    
    estadoUsuario.progresso[estadoUsuario.dataAtual] = {
        dia: diaInicial,
        concluido: false,
        habitos: {}
    };
    
    // SALVAR ambas as configurações
    localStorage.setItem('configuracaoInicial', diaInicial);
    localStorage.setItem('configuracaoJaRealizada', 'true');
    localStorage.setItem('estadoUsuario', JSON.stringify(estadoUsuario));
    
    mostrarFeedback('✅ Configuração concluída! Agora você verá sua rotina automaticamente.');
    
    // Recarregar o sistema normalmente
    setTimeout(() => {
        inicializarSistemaUsuario();
    }, 1500);
}

function carregarEstadoUsuario() {
    const estadoSalvo = localStorage.getItem('estadoUsuario');
    if (estadoSalvo) {
        const estado = JSON.parse(estadoSalvo);
        estadoUsuario = {
            ...estadoUsuario,
            ...estado,
            dataAtual: getLocalDateString()
        };
        
        // Contar dias produtivos ao carregar
        contarDiasProdutivos();
    }
}

function contarDiasProdutivos() {
    let totalDias = 0;
    Object.keys(estadoUsuario.progresso).forEach(data => {
        const progresso = estadoUsuario.progresso[data];
        
        // Conta se fez ALGUM hábito (não precisa ser 100%)
        if (progresso.habitos && Object.keys(progresso.habitos).length > 0) {
            const habitosFeitos = Object.values(progresso.habitos).filter(concluido => concluido).length;
            if (habitosFeitos > 0) {
                totalDias++;
            }
        }
    });
    estadoUsuario.diasProdutivos = totalDias;
}

function determinarDiaAtual() {
    const configuracao = JSON.parse(localStorage.getItem('configuracaoRotinas') || '{}');
    const diasDisponiveis = Object.keys(configuracao.dias || {});
    
    if (diasDisponiveis.length === 0) {
        estadoUsuario.diaAtual = null;
        return;
    }
    
    // Determinar o dia da semana atual
    const data = parseLocalDate(estadoUsuario.dataAtual);
    const diasSemana = ['domingo', 'segunda', 'terca', 'quarta', 'quinta', 'sexta', 'sabado'];
    const diaSemanaAtual = diasSemana[data.getDay()];
    
    // Verificar se temos configuração para este dia
    if (diasDisponiveis.includes(diaSemanaAtual)) {
        estadoUsuario.diaAtual = diaSemanaAtual;
    } else {
        // Se não tem configuração para hoje, usar o primeiro dia disponível
        estadoUsuario.diaAtual = diasDisponiveis[0];
    }
    
    // Registrar o dia de hoje se não existir (já é feito no resetarHabitosNovoDia)
    if (!estadoUsuario.progresso[estadoUsuario.dataAtual]) {
        estadoUsuario.progresso[estadoUsuario.dataAtual] = {
            dia: estadoUsuario.diaAtual,
            concluido: false,
            habitos: {}
        };
    } else {
        estadoUsuario.progresso[estadoUsuario.dataAtual].dia = estadoUsuario.diaAtual;
    }
    
    localStorage.setItem('estadoUsuario', JSON.stringify(estadoUsuario));
}

function configurarEventListenersUsuario() {
    document.getElementById('btn-teste-avancar-dia')?.addEventListener('click', function() {
        const hoje = parseLocalDate(estadoUsuario.dataAtual);
        hoje.setDate(hoje.getDate() + 1);
        estadoUsuario.dataAtual = getLocalDateString(hoje);
        localStorage.setItem('estadoUsuario', JSON.stringify(estadoUsuario));
        
        // IMPORTANTE: Resetar hábitos ao avançar o dia
        resetarHabitosNovoDia();
        
        mostrarFeedback('🚀 TESTE: Data avançada para o próximo dia!');
        determinarDiaAtual();
        carregarRotinaDoDia();
        calcularStreak();
        atualizarProgressoSemanal();
        atualizarContadores();
    });

    document.getElementById('btn-teste-resetar')?.addEventListener('click', function() {
        if (confirm('🚨 TESTE: Resetar TODO o progresso?\n\nIsso irá limpar todas as marcações mas manterá a configuração inicial.')) {
            estadoUsuario.progresso = {};
            estadoUsuario.streak = 0;
            estadoUsuario.diasProdutivos = 0;
            estadoUsuario.recompensasLiberadas = {};
            determinarDiaAtual();
            localStorage.setItem('estadoUsuario', JSON.stringify(estadoUsuario));
            localStorage.setItem('recompensasLiberadas', JSON.stringify({}));
            
            mostrarFeedback('🔄 TESTE: Progresso resetado com sucesso!');
            carregarRotinaDoDia();
            calcularStreak();
            atualizarProgressoSemanal();
            atualizarContadores();
        }
    });

    // Event delegation para checkboxes
    document.addEventListener('change', function(e) {
        if (e.target.classList.contains('checkbox-habito')) {
            const habitoId = e.target.getAttribute('data-habito-id');
            const concluido = e.target.checked;
            
            estadoUsuario.progresso[estadoUsuario.dataAtual].habitos[habitoId] = concluido;
            
            const todosHabitos = document.querySelectorAll('.checkbox-habito');
            const todosConcluidos = Array.from(todosHabitos).every(checkbox => checkbox.checked);
            
            const jaEstavaConcluido = estadoUsuario.progresso[estadoUsuario.dataAtual].concluido;
            estadoUsuario.progresso[estadoUsuario.dataAtual].concluido = todosConcluidos;
            
            // Recalcular dias produtivos SEMPRE que mudar hábitos
            contarDiasProdutivos();

            // Registrar histórico do hábito quando marcado (apenas ao marcar)
            if (concluido) {
                // registra a marcação no histórico (somente se ainda não existir para hoje)
                registrarMarcacaoHabito(habitoId);
            }
            
            if (todosConcluidos && !jaEstavaConcluido) {
                document.getElementById('status-dia').className = 'status-concluido';
                document.getElementById('status-dia').textContent = '✅ Dia Concluído!';
                
                // Liberar recompensa ao concluir tudo
                liberarRecompensa();
                
                if (!estadoUsuario.progresso[estadoUsuario.dataAtual].jaContabilizado) {
                    estadoUsuario.progresso[estadoUsuario.dataAtual].jaContabilizado = true;
                    calcularStreak();
                    atualizarContadores();
                }

                // Salvar estado após conclusão
                localStorage.setItem('estadoUsuario', JSON.stringify(estadoUsuario));
            } else if (!todosConcluidos) {
                const habitosConcluidos = Array.from(todosHabitos).filter(cb => cb.checked).length;
                const progressoPercentual = Math.round((habitosConcluidos / todosHabitos.length) * 100);
                
                document.getElementById('status-dia').className = 'status-andamento';
                document.getElementById('status-dia').textContent = `⏳ ${progressoPercentual}% Concluído`;
                
                if (jaEstavaConcluido) {
                    estadoUsuario.progresso[estadoUsuario.dataAtual].jaContabilizado = false;
                    calcularStreak();
                    atualizarContadores();
                }
            }
            
            // ATUALIZAR O CONTADOR VISUALMENTE IMEDIATAMENTE
            document.getElementById('contador-dias').textContent = estadoUsuario.diasProdutivos;
            
            localStorage.setItem('estadoUsuario', JSON.stringify(estadoUsuario));
            atualizarProgressoSemanal();
        }
    });
}

// NOVA FUNÇÃO: Liberar recompensa quando concluir tudo
function liberarRecompensa() {
    const configuracao = JSON.parse(localStorage.getItem('configuracaoRotinas') || '{}');
    const diaConfig = configuracao.dias[estadoUsuario.diaAtual];
    
    if (diaConfig && diaConfig.recompensa) {
        // Verificar se já liberou a recompensa hoje
        const hoje = estadoUsuario.dataAtual;
        if (!estadoUsuario.recompensasLiberadas[hoje]) {
            estadoUsuario.recompensasLiberadas[hoje] = true;
            localStorage.setItem('recompensasLiberadas', JSON.stringify(estadoUsuario.recompensasLiberadas));
            
            // Mostrar modal de recompensa
            mostrarModalRecompensa(diaConfig.recompensa);
        }
    }
}

// NOVA FUNÇÃO: Modal para mostrar recompensa
function mostrarModalRecompensa(recompensa) {
    const modal = document.createElement('div');
    modal.className = 'modal-recompensa';
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0,0,0,0.8);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 10000;
        animation: fadeIn 0.3s ease;
        padding: 1rem;
    `;
    
    modal.innerHTML = `
        <div class="modal-content" style="
            background: var(--bg-card);
            padding: 1.5rem;
            border-radius: var(--border-radius);
            text-align: center;
            border: 3px solid var(--verde-principal);
            max-width: 400px;
            margin: 1rem;
            animation: scaleIn 0.3s ease;
            width: 100%;
        ">
            <div style="font-size: 3rem; margin-bottom: 1rem;">🎉</div>
            <h2 style="color: var(--verde-principal); margin-bottom: 1rem;">Parabéns!</h2>
            <p style="margin-bottom: 1.5rem; font-size: 1rem;">Você completou todas as tarefas de hoje!</p>
            <div style="
                background: var(--roxo-suave);
                padding: 1rem;
                border-radius: var(--border-radius);
                border-left: 4px solid var(--roxo-principal);
                margin-bottom: 1.5rem;
            ">
                <strong style="color: var(--roxo-principal);">🎁 Recompensa Liberada:</strong>
                <p style="margin-top: 0.5rem; font-size: 1.1rem;">${recompensa}</p>
            </div>
            <button class="btn-principal" id="fechar-modal" style="
                padding: 1rem 2rem;
                font-size: 1rem;
                width: 100%;
            ">
                Continuar
            </button>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Adicionar estilos de animação
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        @keyframes scaleIn {
            from { transform: scale(0.8); opacity: 0; }
            to { transform: scale(1); opacity: 1; }
        }
    `;
    document.head.appendChild(style);
    
    // Fechar modal
    document.getElementById('fechar-modal').addEventListener('click', function() {
        modal.style.animation = 'fadeOut 0.3s ease';
        setTimeout(() => modal.remove(), 300);
    });
    
    // Fechar ao clicar fora
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            modal.style.animation = 'fadeOut 0.3s ease';
            setTimeout(() => modal.remove(), 300);
        }
    });
}

function carregarRotinaDoDia() {
    const configuracao = localStorage.getItem('configuracaoRotinas');
    const container = document.getElementById('conteudo-rotina');
    
    if (!configuracao || !estadoUsuario.diaAtual) {
        container.innerHTML = `
            <div class="mensagem-inicial">
                <h2>Nenhuma rotina configurada</h2>
                <p>As rotinas ainda não foram configuradas.</p>
                <p>Configure suas rotinas na página principal.</p>
            </div>
        `;
        document.getElementById('status-dia').textContent = '❌ Rotina Não Configurada';
        document.getElementById('dia-atual').textContent = '';
        return;
    }
    
    try {
        const config = JSON.parse(configuracao);
    const dia = config.dias[estadoUsuario.diaAtual];
        
        if (!dia || !dia.habitos || dia.habitos.length === 0) {
            container.innerHTML = `
                <div class="mensagem-inicial">
                    <h2>Rotina de ${estadoUsuario.diaAtual} não configurada</h2>
                    <p>A rotina de ${estadoUsuario.diaAtual} está vazia ou não foi configurada.</p>
                    <p>Configure suas rotinas na página principal.</p>
                </div>
            `;
            document.getElementById('status-dia').textContent = '📝 Rotina Vazia';
            document.getElementById('dia-atual').textContent = `Dia: ${estadoUsuario.diaAtual}`;
            return;
        }
        
        // Verificar se já liberou recompensa hoje
        const recompensaLiberada = estadoUsuario.recompensasLiberadas[estadoUsuario.dataAtual];
        
        let html = `
            <div class="cabecalho-treino">
                <h2>${dia.nome}</h2>
                <p>${formatarData(estadoUsuario.dataAtual)}</p>
                ${dia.recompensa ? `
                    <div class="recompensa-info ${recompensaLiberada ? 'recompensa-liberada' : ''}">
                        ${recompensaLiberada ? '✅ ' : '🎁 '}${dia.recompensa}
                        ${recompensaLiberada ? '<small>✓ Recompensa liberada!</small>' : '<small>Complete todas as tarefas para liberar</small>'}
                    </div>
                ` : ''}
            </div>
            <div class="lista-exercicios-aluno">
        `;
        
        const progressoDia = estadoUsuario.progresso[estadoUsuario.dataAtual];
        let habitosConcluidos = 0;
        
        dia.habitos.forEach((habito, index) => {
            const habitoId = `${estadoUsuario.diaAtual}-${index}`;
            const concluido = progressoDia?.habitos?.[habitoId] || false;
            if (concluido) habitosConcluidos++;
            
            html += `
                <div class="exercicio-aluno ${concluido ? 'concluido' : ''}">
                    <div class="exercicio-info">
                        <h3>${habito.nome}</h3>
                        ${habito.dica ? `<p class="dica-exercicio">💡 ${habito.dica}</p>` : ''}
                        <button class="btn-historico" onclick="carregarHistoricoHabito('${habitoId}'); abrirDrawerHistorico();">
                            <i class="fas fa-history"></i> Ver histórico
                        </button>
                    </div>
                    <div class="exercicio-controle">
                        <label class="checkbox-container">
                            <input type="checkbox" class="checkbox-habito" data-habito-id="${habitoId}" ${concluido ? 'checked' : ''}>
                            <span class="checkmark"></span>
                        </label>
                    </div>
                </div>
            `;
        });
        
        html += '</div>';
        
        const progressoPercentual = dia.habitos.length > 0 ? Math.round((habitosConcluidos / dia.habitos.length) * 100) : 0;
        
        if (progressoDia?.concluido) {
            document.getElementById('status-dia').className = 'status-concluido';
            document.getElementById('status-dia').textContent = '✅ Dia Concluído!';
        } else if (habitosConcluidos > 0) {
            document.getElementById('status-dia').className = 'status-andamento';
            document.getElementById('status-dia').textContent = `⏳ ${progressoPercentual}% Concluído`;
        } else {
            document.getElementById('status-dia').className = 'status-pendente';
            document.getElementById('status-dia').textContent = '📝 Dia Pendente';
        }
        
        document.getElementById('dia-atual').textContent = `${dia.nome}`;
        container.innerHTML = html;
        
    } catch (error) {
        console.error('Erro ao carregar rotina:', error);
        container.innerHTML = `
            <div class="mensagem-inicial">
                <h2>Erro ao carregar rotina</h2>
                <p>Ocorreu um erro ao carregar sua rotina atual.</p>
                <p>Tente recarregar a página.</p>
            </div>
        `;
    }
}

function atualizarProgressoSemanal() {
    const dias = ['domingo', 'segunda', 'terca', 'quarta', 'quinta', 'sexta', 'sabado'];
    let diasCompletos = 0;
    
    const dataAtualObj = parseLocalDate(estadoUsuario.dataAtual);
    const inicioSemana = new Date(dataAtualObj);
    inicioSemana.setDate(dataAtualObj.getDate() - dataAtualObj.getDay());
    
    const configuracao = JSON.parse(localStorage.getItem('configuracaoRotinas') || '{}');
    
    dias.forEach((dia, index) => {
        const dataDia = new Date(inicioSemana);
        dataDia.setDate(inicioSemana.getDate() + index);
        const dataStr = getLocalDateString(dataDia);
        
        const progressoDia = estadoUsuario.progresso[dataStr];
        let percentual = 0;
        let mostrarFaltou = false;
        
        // Verificar se é um dia passado
    const hoje = parseLocalDate(estadoUsuario.dataAtual);
    const diaDaSemana = parseLocalDate(dataStr);
    const ehDiaPassado = diaDaSemana < hoje;
        
        // Calcular percentual baseado na rotina real do dia
        if (progressoDia && progressoDia.dia) {
            const rotinaDoDia = configuracao.dias?.[progressoDia.dia];
            if (rotinaDoDia && rotinaDoDia.habitos) {
                const totalHabitos = rotinaDoDia.habitos.length;
                if (totalHabitos > 0) {
                    let habitosConcluidos = 0;
                    rotinaDoDia.habitos.forEach((habito, index) => {
                        const habitoId = `${progressoDia.dia}-${index}`;
                        if (progressoDia.habitos?.[habitoId]) {
                            habitosConcluidos++;
                        }
                    });
                    percentual = Math.round((habitosConcluidos / totalHabitos) * 100);
                }
            }
        }
        
        // Se é um dia passado sem nenhum progresso E tem rotina definida, marca como "faltou"
        if (ehDiaPassado && percentual === 0 && progressoDia?.dia) {
            mostrarFaltou = true;
        }
        
        // Se a rotina está marcada como concluída, forçar 100%
        if (progressoDia?.concluido) {
            percentual = 100;
            mostrarFaltou = false;
        }
        
        const rotinaDoDia = progressoDia?.dia || '-';
        const nomeRotina = configuracao.dias && configuracao.dias[rotinaDoDia] ? configuracao.dias[rotinaDoDia].nome : '-';
        
        const elementoDia = document.querySelector(`.dia-progresso[data-dia="${dia}"]`);
        if (elementoDia) {
            const barra = elementoDia.querySelector('.progresso');
            const percentualElem = elementoDia.querySelector('.percentual');
            const nomeRotinaElem = elementoDia.querySelector('.nome-treino');
            
            barra.style.width = `${percentual}%`;
            
            if (mostrarFaltou) {
                percentualElem.textContent = '❌ Faltou';
                percentualElem.className = 'percentual faltou';
            } else {
                percentualElem.textContent = `${percentual}%`;
                percentualElem.className = 'percentual';
            }
            
            nomeRotinaElem.textContent = nomeRotina;
            
            elementoDia.classList.remove('completo', 'parcial', 'vazio', 'faltou', 'ativo');
            
            if (percentual === 100) {
                elementoDia.classList.add('completo');
                diasCompletos++;
            } else if (percentual > 0) {
                elementoDia.classList.add('parcial');
            } else if (mostrarFaltou) {
                elementoDia.classList.add('faltou');
            } else {
                elementoDia.classList.add('vazio');
            }
            
            if (dataStr === estadoUsuario.dataAtual) {
                elementoDia.classList.add('ativo');
            }
        }
    });
    
    document.getElementById('contador-semanal').textContent = `${diasCompletos}/7 dias completos esta semana`;
}

function calcularStreak() {
    const hoje = parseLocalDate(estadoUsuario.dataAtual);
    let streakAtual = 0;
    let dataVerificar = new Date(hoje);
    
    // Verificar de hoje para trás
    for (let i = 0; i < 365; i++) {
    const dataStr = getLocalDateString(dataVerificar);
        const progressoDia = estadoUsuario.progresso[dataStr];
        
        // Se o dia tem rotina definida
        if (progressoDia && progressoDia.dia) {
            const ehDiaPassado = dataVerificar < hoje;
            const ehDiaAtual = dataStr === estadoUsuario.dataAtual;
            
            // Se foi 100% concluído, aumenta o streak
            if (progressoDia.concluido) {
                streakAtual++;
            } 
            // Se é um dia PASSADO com rotina mas 0% feito = FALTOU (quebra streak)
            else if (ehDiaPassado && !progressoDia.concluido) {
                // Verificar se realmente não fez NADA (0%)
                let percentual = 0;
                const configuracao = JSON.parse(localStorage.getItem('configuracaoRotinas') || '{}');
                const rotinaDoDia = configuracao.dias?.[progressoDia.dia];
                
                if (rotinaDoDia && rotinaDoDia.habitos) {
                    let habitosConcluidos = 0;
                    rotinaDoDia.habitos.forEach((habito, index) => {
                        const habitoId = `${progressoDia.dia}-${index}`;
                        if (progressoDia.habitos?.[habitoId]) {
                            habitosConcluidos++;
                        }
                    });
                    percentual = Math.round((habitosConcluidos / rotinaDoDia.habitos.length) * 100);
                }
                
                // Se fez 0% em dia PASSADO = FALTOU (quebra streak)
                if (percentual === 0) {
                    break;
                }
                // Se fez algo (1-99%) em dia PASSADO, não quebra (só não aumenta)
            }
            // Se é o dia ATUAL e não concluiu ainda, não quebra (ainda pode fazer)
            else if (ehDiaAtual && !progressoDia.concluido) {
                // Não quebra - usuário ainda pode fazer a rotina hoje
            }
        }
        // Se não tem rotina definida, continua verificando
        
        // Voltar um dia
        dataVerificar.setDate(dataVerificar.getDate() - 1);
    }
    
    estadoUsuario.streak = streakAtual;
    localStorage.setItem('estadoUsuario', JSON.stringify(estadoUsuario));
    console.log('Streak calculado:', streakAtual, 'dias');
}

function atualizarContadores() {
    document.getElementById('contador-dias').textContent = estadoUsuario.diasProdutivos;
    document.getElementById('streak-atual').textContent = `🔥 Sequência atual: ${estadoUsuario.streak} dias`;
}

function formatarData(dataStr) {
    const data = parseLocalDate(dataStr);
    return data.toLocaleDateString('pt-BR', { 
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

function mostrarFeedback(mensagem) {
    const feedback = document.createElement('div');
    feedback.className = 'feedback-temporario';
    feedback.textContent = mensagem;
    document.body.appendChild(feedback);
    
    setTimeout(() => {
        feedback.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => feedback.remove(), 300);
    }, 3000);
}

/* ===== HISTÓRICO DE HÁBITOS (localStorage) ===== */
function registrarMarcacaoHabito(habitoId) {
    try {
        const key = 'historicoHabitos';
        const hoje = estadoUsuario.dataAtual || getLocalDateString();
        const raw = localStorage.getItem(key) || '{}';
        const historico = JSON.parse(raw);

        if (!historico[habitoId]) {
            historico[habitoId] = { marcacoes: {}, startDate: hoje };
        }

        // Garantir startDate
        if (!historico[habitoId].startDate) historico[habitoId].startDate = hoje;

        // Registrar apenas se ainda não registrado hoje
        if (!historico[habitoId].marcacoes[hoje]) {
            historico[habitoId].marcacoes[hoje] = true;
        }

        // Ajustar startDate para a mais antiga
        const sd = historico[habitoId].startDate;
        if (parseLocalDate(hoje) < parseLocalDate(sd)) {
            historico[habitoId].startDate = hoje;
        }

        localStorage.setItem(key, JSON.stringify(historico));
    } catch (err) {
        console.error('Erro ao registrar histórico do hábito:', err);
    }
}

function carregarHistoricoHabito(habitoId) {
    const key = 'historicoHabitos';
    const raw = localStorage.getItem(key) || '{}';
    const historico = JSON.parse(raw);
    const dados = historico[habitoId] || { marcacoes: {}, startDate: null };

    // Tentar obter nome do hábito a partir da configuração atual
    let nomeHabito = habitoId;
    try {
        const config = JSON.parse(localStorage.getItem('configuracaoRotinas') || '{}');
        const parts = habitoId.split('-');
        const index = Number(parts.pop());
        const dia = parts.join('-');
        nomeHabito = config.dias?.[dia]?.habitos?.[index]?.nome || habitoId;
    } catch (e) {
        // fallback
    }

    // Preencher o painel
    const titulo = document.getElementById('hist-titulo');
    const contador = document.getElementById('hist-contador');
    const lista = document.getElementById('hist-lista');

    if (titulo) titulo.textContent = nomeHabito;

    const marcacoes = Object.keys(dados.marcacoes || {}).sort((a,b) => parseLocalDate(b) - parseLocalDate(a));
    const total = marcacoes.length;
    const start = dados.startDate || '--';
    const startFmt = start !== '--' ? parseLocalDate(start).toLocaleDateString('pt-BR') : '--/--/----';

    if (contador) contador.textContent = `${total} / 365 dias desde ${startFmt}`;
    if (lista) {
        lista.innerHTML = '';
        if (marcacoes.length === 0) {
            lista.innerHTML = `<div class="item-data">Nenhuma marcação registrada ainda.</div>`;
        } else {
            marcacoes.forEach(d => {
                const dFmt = parseLocalDate(d).toLocaleDateString('pt-BR');
                const el = document.createElement('div');
                el.className = 'item-data';
                el.textContent = dFmt;
                lista.appendChild(el);
            });
        }
    }
}

function abrirDrawerHistorico() {
    const drawer = document.getElementById('drawer-historico');
    if (!drawer) return;
    drawer.setAttribute('aria-hidden', 'false');
}

function fecharDrawerHistorico() {
    const drawer = document.getElementById('drawer-historico');
    if (!drawer) return;
    drawer.setAttribute('aria-hidden', 'true');
}

// Fechar drawer ao clicar no botão / backdrop
document.addEventListener('DOMContentLoaded', () => {
    const btnFechar = document.getElementById('fechar-historico');
    const backdrop = document.getElementById('drawer-backdrop');
    if (btnFechar) btnFechar.addEventListener('click', fecharDrawerHistorico);
    if (backdrop) backdrop.addEventListener('click', fecharDrawerHistorico);
});