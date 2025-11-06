// Base de dados de hábitos COMPLETA e REORGANIZADA
const dadosHabit = {
    categorias: {
        saude: {
            nome: "💊 SAÚDE",
            habitos: [
                { nome: "Beber 2L de água", dica: "Use um aplicativo para lembrar" },
                { nome: "Dormir 8 horas", dica: "Desligue telas 1h antes de dormir" },
                { nome: "Tomar vitaminas", dica: "Deixe na mesa para não esquecer" },
                { nome: "Meditar 10min", dica: "Use um app como guia" },
                { nome: "Alongamento matinal", dica: "Faça ao acordar" },
                { nome: "Caminhar 30min", dica: "Pode ser durante o almoço" },
                { nome: "Comer frutas", dica: "Inclua em todas as refeições" },
                { nome: "Evitar açúcar", dica: "Substitua por frutas" },
                { nome: "Postura correta", dica: "Lembre-se de sentar direito" },
                { nome: "Respiração consciente", dica: "Pratique durante pausas" }
            ]
        },
        produtividade: {
            nome: "⚡ PRODUTIVIDADE",
            habitos: [
                { nome: "Planejar o dia", dica: "Faça antes de começar a trabalhar" },
                { nome: "Técnica Pomodoro", dica: "25min trabalho + 5min pausa" },
                { nome: "Limpar email", dica: "Reserve 15min por dia" },
                { nome: "Organizar workspace", dica: "Mantenha a mesa limpa" },
                { nome: "Priorizar tarefas", dica: "Use a matriz Eisenhower" },
                { nome: "Revisar metas semanais", dica: "Todas as segundas-feiras" },
                { nome: "Aprender algo novo", dica: "15min de estudo por dia" },
                { nome: "Fazer pausas ativas", dica: "Levante a cada hora" },
                { nome: "Desligar notificações", dica: "Durante trabalho focado" },
                { nome: "Revisar progresso", dica: "No final do dia" }
            ]
        },
        mental: {
            nome: "🧠 SAÚDE MENTAL",
            habitos: [
                { nome: "Diário de gratidão", dica: "Escreva 3 coisas boas do dia" },
                { nome: "Respiração profunda", dica: "4-7-8: inspire 4s, segure 7s, expire 8s" },
                { nome: "Limitar redes sociais", dica: "Máximo 1h por dia" },
                { nome: "Praticar mindfulness", dica: "Esteja presente no momento" },
                { nome: "Ler um livro", dica: "20min antes de dormir" },
                { nome: "Fazer algo criativo", dica: "Desenhe, escreva, crie" },
                { nome: "Contato com natureza", dica: "Passeie em um parque" },
                { nome: "Desconectar digital", dica: "1h sem telas por dia" },
                { nome: "Auto-cuidado", dica: "Cuide de você mesmo" },
                { nome: "Refletir sobre emoções", dica: "Identifique como se sente" }
            ]
        },
        fitness: {
            nome: "🏃‍♂️ FITNESS",
            habitos: [
                { nome: "Exercício matinal", dica: "Acorde 30min mais cedo" },
                { nome: "Yoga 15min", dica: "Use vídeos do YouTube" },
                { nome: "Subir escadas", dica: "Evite elevador" },
                { nome: "Treino de força", dica: "3x por semana" },
                { nome: "Cardio 30min", dica: "Corrida, bike ou natação" },
                { nome: "Alongamento pós-treino", dica: "Previne lesões" },
                { nome: "Caminhada após almoço", dica: "Ajuda na digestão" },
                { nome: "Exercícios em casa", dica: "Use o peso do corpo" },
                { nome: "Alongamento noturno", dica: "Relaxa antes de dormir" },
                { nome: "Atividade ao ar livre", dica: "Combine exercício e natureza" }
            ]
        },
        aprendizado: {
            nome: "📚 APRENDIZADO",
            habitos: [
                { nome: "Ler notícias", dica: "Fique informado" },
                { nome: "Estudar idioma", dica: "15min por dia com app" },
                { nome: "Assistir documentário", dica: "Aprendizado visual" },
                { nome: "Praticar nova habilidade", dica: "Programação, música, etc" },
                { nome: "Fazer curso online", dica: "Dedicar 30min por dia" },
                { nome: "Ler artigo técnico", dica: "Mantenha-se atualizado" },
                { nome: "Resolver puzzles", dica: "Desafie seu cérebro" },
                { nome: "Escrever resumo", dica: "Fixe o conhecimento" },
                { nome: "Participar de fórum", dica: "Troque conhecimentos" },
                { nome: "Ensinar alguém", dica: "A melhor forma de aprender" }
            ]
        },
        social: {
            nome: "👥 SOCIAL",
            habitos: [
                { nome: "Ligar para família", dica: "Mantenha contato" },
                { nome: "Encontrar amigos", dica: "Presencial é melhor" },
                { nome: "Fazer networking", dica: "Conecte-se com colegas" },
                { nome: "Ajudar alguém", dica: "Pequenos gestos contam" },
                { nome: "Participar comunidade", dica: "Grupos locais ou online" },
                { nome: "Expressar gratidão", dica: "Diga obrigado" },
                { nome: "Ouvir ativamente", dica: "Esteja presente na conversa" },
                { nome: "Fazer voluntariado", dica: "Ajude sua comunidade" },
                { nome: "Compartilhar conhecimento", dica: "Ensine o que sabe" },
                { nome: "Celebrar conquistas", dica: "Suas e dos outros" }
            ]
        },
        casa: {
            nome: "🏠 CASA",
            habitos: [
                { nome: "Fazer a cama", dica: "Comece o dia organizado" },
                { nome: "Lavar louça", dica: "Não acumule" },
                { nome: "Organizar um cômodo", dica: "15min por dia" },
                { nome: "Lavar roupas", dica: "Não deixe acumular" },
                { nome: "Cozinhar comida saudável", dica: "Planeje as refeições" },
                { nome: "Limpar geladeira", dica: "Uma vez por semana" },
                { nome: "Regar plantas", dica: "Cuide da natureza em casa" },
                { nome: "Arrumar armário", dica: "Doe o que não usa" },
                { nome: "Limpar banheiro", dica: "Mantenha higiene" },
                { nome: "Passar pano no chão", dica: "Sujeira invisível também conta" }
            ]
        },
        financeiro: {
            nome: "💰 FINANCEIRO",
            habitos: [
                { nome: "Registrar gastos", dica: "Use um app ou caderno" },
                { nome: "Poupar 10%", dica: "Automático é melhor" },
                { nome: "Revisar orçamento", dica: "Acompanhe mensalmente" },
                { nome: "Evitar compras impulsivas", dica: "Espere 24h antes de comprar" },
                { nome: "Investir regularmente", dica: "Consistência é a chave" },
                { nome: "Pagar contas em dia", dica: "Evite juros" },
                { nome: "Comparar preços", dica: "Pesquise antes de comprar" },
                { nome: "Estudar educação financeira", dica: "Conhecimento é poder" },
                { nome: "Definir metas financeiras", dica: "Onde quer chegar?" },
                { nome: "Avalizar necessidades vs desejos", dica: "Pense antes de gastar" }
            ]
        }
    }
};

// Inicialização do Sistema de Hábitos
document.addEventListener('DOMContentLoaded', function() {
    inicializarSistemaHabit();
});

function inicializarSistemaHabit() {
    carregarBibliotecaHabitos();
    carregarBibliotecaPessoal();
    configurarEventListenersHabit();
    carregarConfiguracaoSalva();
    configurarAbas();
    inicializarFiltros(); // ← ADICIONAR ESTA LINHA
}

function configurarAbas() {
    const abas = document.querySelectorAll('.aba');
    const conteudos = document.querySelectorAll('.conteudo-aba');
    
    abas.forEach(aba => {
        aba.addEventListener('click', function() {
            const abaAlvo = this.getAttribute('data-aba');
            
            abas.forEach(a => a.classList.remove('ativa'));
            conteudos.forEach(c => c.classList.remove('ativa'));
            
            this.classList.add('ativa');
            document.getElementById(abaAlvo).classList.add('ativa');
        });
    });
}

function carregarBibliotecaHabitos() {
    const lista = document.getElementById('lista-habitos');
    if (!lista) return;
    
    let html = '';
    
    Object.keys(dadosHabit.categorias).forEach(categoria => {
        const categoriaData = dadosHabit.categorias[categoria];
        html += `
            <div class="grupo-biblioteca" data-categoria="${categoria}">
                <h5>${categoriaData.nome}</h5>
                <div class="exercicios-grupo">
                    ${categoriaData.habitos.map((habito, index) => `
                        <div class="exercicio-biblioteca" data-categoria="${categoria}" data-index="${index}" data-tipo="principal">
                            <div class="exercicio-info">
                                <strong>${habito.nome}</strong>
                                ${habito.dica ? `<div class="exercicio-dica">💡 ${habito.dica}</div>` : ''}
                            </div>
                            <div class="exercicio-acoes">
                                <button class="btn-add" data-dia="segunda" data-categoria="${categoria}" data-index="${index}" data-tipo="principal">Seg</button>
                                <button class="btn-add" data-dia="terca" data-categoria="${categoria}" data-index="${index}" data-tipo="principal">Ter</button>
                                <button class="btn-add" data-dia="quarta" data-categoria="${categoria}" data-index="${index}" data-tipo="principal">Qua</button>
                                <button class="btn-add" data-dia="quinta" data-categoria="${categoria}" data-index="${index}" data-tipo="principal">Qui</button>
                                <button class="btn-add" data-dia="sexta" data-categoria="${categoria}" data-index="${index}" data-tipo="principal">Sex</button>
                                <button class="btn-add" data-dia="sabado" data-categoria="${categoria}" data-index="${index}" data-tipo="principal">Sáb</button>
                                <button class="btn-add" data-dia="domingo" data-categoria="${categoria}" data-index="${index}" data-tipo="principal">Dom</button>
                            </div>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    });
    
    lista.innerHTML = html;
}

function carregarBibliotecaPessoal() {
    const lista = document.getElementById('lista-habitos-pessoal');
    if (!lista) return;
    
    const habitosPessoais = JSON.parse(localStorage.getItem('habitosPessoais') || '[]');
    
    if (habitosPessoais.length === 0) {
        lista.innerHTML = '<p class="sem-exercicios">Nenhum hábito pessoal adicionado ainda.</p>';
        return;
    }
    
    let html = '';
    
    // Agrupar hábitos por categoria
    const habitosPorCategoria = {};
    habitosPessoais.forEach((habito, index) => {
        if (!habitosPorCategoria[habito.categoria]) {
            habitosPorCategoria[habito.categoria] = [];
        }
        habitosPorCategoria[habito.categoria].push({...habito, index});
    });
    
    Object.keys(habitosPorCategoria).forEach(categoria => {
        const categoriaData = dadosHabit.categorias[categoria] || { nome: categoria };
        html += `
            <div class="grupo-biblioteca" data-categoria="${categoria}">
                <h5>${categoriaData.nome}</h5>
                <div class="exercicios-grupo">
                    ${habitosPorCategoria[categoria].map(habito => `
                        <div class="exercicio-biblioteca" data-categoria="${habito.categoria}" data-index="${habito.index}" data-tipo="pessoal">
                            <div class="exercicio-info">
                                <strong>${habito.nome}</strong>
                                ${habito.dica ? `<div class="exercicio-dica">💡 ${habito.dica}</div>` : ''}
                            </div>
                            <div class="exercicio-acoes">
                                <button class="btn-add" data-dia="segunda" data-categoria="${habito.categoria}" data-index="${habito.index}" data-tipo="pessoal">Seg</button>
                                <button class="btn-add" data-dia="terca" data-categoria="${habito.categoria}" data-index="${habito.index}" data-tipo="pessoal">Ter</button>
                                <button class="btn-add" data-dia="quarta" data-categoria="${habito.categoria}" data-index="${habito.index}" data-tipo="pessoal">Qua</button>
                                <button class="btn-add" data-dia="quinta" data-categoria="${habito.categoria}" data-index="${habito.index}" data-tipo="pessoal">Qui</button>
                                <button class="btn-add" data-dia="sexta" data-categoria="${habito.categoria}" data-index="${habito.index}" data-tipo="pessoal">Sex</button>
                                <button class="btn-add" data-dia="sabado" data-categoria="${habito.categoria}" data-index="${habito.index}" data-tipo="pessoal">Sáb</button>
                                <button class="btn-add" data-dia="domingo" data-categoria="${habito.categoria}" data-index="${habito.index}" data-tipo="pessoal">Dom</button>
                                <button class="btn-remover-pessoal" data-index="${habito.index}">🗑️</button>
                            </div>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    });
    
    lista.innerHTML = html;
}
function configurarEventListenersHabit() {
    // Botões de adicionar hábito
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-add')) {
            const dia = e.target.getAttribute('data-dia');
            const categoria = e.target.getAttribute('data-categoria');
            const index = e.target.getAttribute('data-index');
            const tipo = e.target.getAttribute('data-tipo');
            adicionarHabitoDia(dia, categoria, parseInt(index), tipo);
        }
        
        if (e.target.classList.contains('btn-remover-pessoal')) {
            const index = e.target.getAttribute('data-index');
            removerHabitoPessoal(index);
        }
    });
    
    // Botões limpar
    document.querySelectorAll('.btn-limpar').forEach(btn => {
        btn.addEventListener('click', function() {
            const dia = this.getAttribute('data-treino');
            limparDia(dia);
        });
    });
    
    // Ações principais
    document.getElementById('btn-salvar')?.addEventListener('click', salvarConfiguracao);
    document.getElementById('btn-visualizar')?.addEventListener('click', visualizarMinhasRotinas);
    document.getElementById('btn-resetar')?.addEventListener('click', resetarTudo);
    
    // Event delegation para inputs e remoção
    document.addEventListener('input', function(e) {
        if (e.target.classList.contains('nome-treino') || e.target.classList.contains('recompensa-dia')) {
            atualizarPreview();
        }
    });
    
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-remover-exercicio')) {
            const habitoElem = e.target.closest('.exercicio-selecionado');
            const dia = e.target.closest('.exercicios-lista').getAttribute('data-grupo');
            habitoElem.remove();
            atualizarContador(dia);
        }
    });
    
    // Filtros - CORREÇÃO: Event listeners simplificados
    document.getElementById('filtro-categoria')?.addEventListener('change', filtrarHabitos);
    document.getElementById('busca-habito')?.addEventListener('input', filtrarHabitos);
    document.getElementById('btn-limpar-filtros')?.addEventListener('click', function() {
        document.getElementById('filtro-categoria').value = '';
        document.getElementById('busca-habito').value = '';
        filtrarHabitos();
        mostrarFeedback('🧹 Filtros limpos');
    });
    
    // Adicionar hábito pessoal
    document.getElementById('btn-adicionar-pessoal')?.addEventListener('click', adicionarHabitoPessoal);
    
    // Feedback visual para busca ativa
    document.getElementById('busca-habito')?.addEventListener('input', function() {
        if (this.value.trim()) {
            this.classList.add('busca-ativa');
        } else {
            this.classList.remove('busca-ativa');
        }
    });
}
// CORREÇÃO: Função de filtro funcionando corretamente
// VERSÃO MELHORADA: Função de filtro com melhor feedback
function filtrarHabitos() {
    const filtroCategoria = document.getElementById('filtro-categoria').value;
    const busca = document.getElementById('busca-habito').value.toLowerCase().trim();
    
    console.log('Filtrando - Categoria:', filtroCategoria, 'Busca:', busca);
    
    let totalVisiveis = 0;
    
    // Primeiro, mostrar TODOS os hábitos
    document.querySelectorAll('.exercicio-biblioteca').forEach(habitoElem => {
        habitoElem.style.display = 'flex';
    });
    
    // Aplicar filtros apenas se houver algum ativo
    if (filtroCategoria || busca) {
        document.querySelectorAll('.exercicio-biblioteca').forEach(habitoElem => {
            const nomeHabito = habitoElem.querySelector('strong').textContent.toLowerCase();
            const categoriaHabito = habitoElem.getAttribute('data-categoria');
            let deveMostrar = true;
            
            // Filtro por categoria
            if (filtroCategoria && categoriaHabito !== filtroCategoria) {
                deveMostrar = false;
            }
            
            // Filtro por busca (só aplicar se ainda deve mostrar)
            if (deveMostrar && busca && !nomeHabito.includes(busca)) {
                deveMostrar = false;
            }
            
            habitoElem.style.display = deveMostrar ? 'flex' : 'none';
            
            if (deveMostrar) {
                totalVisiveis++;
            }
        });
    } else {
        // Se não há filtros, contar todos como visíveis
        totalVisiveis = document.querySelectorAll('.exercicio-biblioteca').length;
    }
    
    // Mostrar/ocultar grupos baseado nos hábitos visíveis
    document.querySelectorAll('.grupo-biblioteca').forEach(grupoElem => {
        const habitosNoGrupo = grupoElem.querySelectorAll('.exercicio-biblioteca');
        const habitosVisiveis = grupoElem.querySelectorAll('.exercicio-biblioteca[style="display: flex"]');
        
        // Se não há filtros ativos ou se há hábitos visíveis no grupo, mostrar o grupo
        const mostrarGrupo = (!filtroCategoria && !busca) || habitosVisiveis.length > 0;
        grupoElem.style.display = mostrarGrupo ? 'block' : 'none';
    });
    
    // Mostrar mensagem se não houver resultados
    const lista = document.getElementById('lista-habitos');
    let mensagemExistente = lista.querySelector('.sem-resultados');
    
    if (totalVisiveis === 0 && (filtroCategoria || busca)) {
        if (!mensagemExistente) {
            mensagemExistente = document.createElement('div');
            mensagemExistente.className = 'sem-resultados';
            mensagemExistente.innerHTML = `
                <i class="fas fa-search"></i>
                <p>Nenhum hábito encontrado</p>
                <small>Tente alterar os filtros ou a busca</small>
            `;
            lista.appendChild(mensagemExistente);
        }
    } else if (mensagemExistente) {
        mensagemExistente.remove();
    }
    
    console.log(`Resultado: ${totalVisiveis} hábitos visíveis`);
}

// Função para inicializar os filtros corretamente
function inicializarFiltros() {
    // Aplicar filtro inicial se houver valores nos campos
    setTimeout(() => {
        const filtroCategoria = document.getElementById('filtro-categoria').value;
        const busca = document.getElementById('busca-habito').value;
        
        if (filtroCategoria || busca) {
            filtrarHabitos();
        }
    }, 100);
}


function adicionarHabitoDia(dia, categoria, index, tipo) {
    let habito;
    
    if (tipo === 'principal') {
        habito = dadosHabit.categorias[categoria].habitos[index];
    } else {
        const habitosPessoais = JSON.parse(localStorage.getItem('habitosPessoais') || '[]');
        habito = habitosPessoais[index];
    }
    
    const lista = document.querySelector(`.exercicios-lista[data-grupo="${dia}"]`);
    
    if (!lista) return;
    
    const habitoElem = document.createElement('div');
    habitoElem.className = 'exercicio-selecionado';
    habitoElem.innerHTML = `
        <div>
            <strong>${habito.nome}</strong>
            ${habito.dica ? `<div class="exercicio-dica">💡 ${habito.dica}</div>` : ''}
        </div>
        <div class="exercicio-config">
            <button class="btn-remover-exercicio">🗑️</button>
        </div>
    `;
    
    lista.appendChild(habitoElem);
    atualizarContador(dia);
}

function adicionarHabitoPessoal() {
    const nome = document.getElementById('novo-habito-nome').value.trim();
    const categoria = document.getElementById('novo-habito-categoria').value;
    const dica = document.getElementById('novo-habito-dica').value.trim();
    
    if (!nome) {
        mostrarFeedback('❌ Preencha o nome do hábito!');
        return;
    }
    
    const habitosPessoais = JSON.parse(localStorage.getItem('habitosPessoais') || '[]');
    
    const novoHabito = {
        nome,
        categoria,
        dica: dica || ''
    };
    
    habitosPessoais.push(novoHabito);
    localStorage.setItem('habitosPessoais', JSON.stringify(habitosPessoais));
    
    // Limpar campos
    document.getElementById('novo-habito-nome').value = '';
    document.getElementById('novo-habito-dica').value = '';
    
    // Recarregar biblioteca pessoal
    carregarBibliotecaPessoal();
    mostrarFeedback('✅ Hábito pessoal adicionado com sucesso!');
}

function removerHabitoPessoal(index) {
    if (!confirm('Tem certeza que deseja remover este hábito?')) return;
    
    const habitosPessoais = JSON.parse(localStorage.getItem('habitosPessoais') || '[]');
    habitosPessoais.splice(index, 1);
    localStorage.setItem('habitosPessoais', JSON.stringify(habitosPessoais));
    
    carregarBibliotecaPessoal();
    mostrarFeedback('✅ Hábito pessoal removido com sucesso!');
}

function atualizarContador(dia) {
    const lista = document.querySelector(`.exercicios-lista[data-grupo="${dia}"]`);
    const contador = document.querySelector(`.grupo[data-grupo="${dia}"] .contador`);
    
    if (lista && contador) {
        const count = lista.children.length;
        contador.textContent = `${count} hábito${count !== 1 ? 's' : ''}`;
    }
}

function limparDia(dia) {
    const lista = document.querySelector(`.exercicios-lista[data-grupo="${dia}"]`);
    if (lista) {
        lista.innerHTML = '';
        atualizarContador(dia);
    }
}

function salvarConfiguracao() {
    const configuracao = {
        dias: {},
    };
    
    // Salvar os dias configurados
    const dias = ['segunda', 'terca', 'quarta', 'quinta', 'sexta', 'sabado', 'domingo'];
    
    dias.forEach(dia => {
        const lista = document.querySelector(`.exercicios-lista[data-grupo="${dia}"]`);
        const nomeInput = document.querySelector(`.grupo[data-grupo="${dia}"] .nome-treino`);
        const recompensaInput = document.querySelector(`.grupo[data-grupo="${dia}"] .recompensa-dia`);
        
        if (lista && lista.children.length > 0) {
            const habitos = Array.from(lista.children).map(habitoElem => {
                return {
                    nome: habitoElem.querySelector('strong').textContent,
                    dica: habitoElem.querySelector('.exercicio-dica')?.textContent.replace('💡 ', '') || ''
                };
            });
            
            configuracao.dias[dia] = {
                nome: nomeInput ? nomeInput.value : `Rotina de ${dia}`,
                recompensa: recompensaInput ? recompensaInput.value : '',
                habitos: habitos
            };
        }
    });
    
    localStorage.setItem('configuracaoRotinas', JSON.stringify(configuracao));
    mostrarFeedback('✅ Configuração salva com sucesso!');
    atualizarPreview();
}

function visualizarMinhasRotinas() {
    window.open('usuario.html', '_blank');
}

function resetarTudo() {
    if (confirm('Tem certeza que deseja resetar TODAS as rotinas configuradas?\n\nIsso irá:\n• Limpar todas as rotinas da semana\n• Manter o progresso do usuário\n• Manter a configuração inicial')) {
        localStorage.removeItem('configuracaoRotinas');
        
        // Limpar apenas as rotinas na interface
        const dias = ['segunda', 'terca', 'quarta', 'quinta', 'sexta', 'sabado', 'domingo'];
        dias.forEach(limparDia);
        
        // Resetar os nomes padrão das rotinas
        document.querySelectorAll('.nome-treino').forEach(input => {
            const dia = input.closest('.grupo').getAttribute('data-grupo');
            const nomesPadrao = {
                'segunda': 'Rotina de Início de Semana',
                'terca': 'Foco no Trabalho',
                'quarta': 'Dia de Equilíbrio',
                'quinta': 'Preparação para o Fim de Semana',
                'sexta': 'Finalização da Semana',
                'sabado': 'Descanso Ativo',
                'domingo': 'Preparação para a Semana'
            };
            input.value = nomesPadrao[dia] || `Rotina de ${dia}`;
        });
        
        // Resetar recompensas padrão
        document.querySelectorAll('.recompensa-dia').forEach(input => {
            const dia = input.closest('.grupo').getAttribute('data-grupo');
            const recompensasPadrao = {
                'segunda': 'Assistir série',
                'terca': 'Jogar videogame',
                'quarta': 'Comer sobremesa',
                'quinta': 'Pedir comida',
                'sexta': 'Sair com amigos',
                'sabado': 'Dormir até tarde',
                'domingo': 'Assistir filme'
            };
            input.value = recompensasPadrao[dia] || '';
        });
        
        mostrarFeedback('🔄 Todas as rotinas foram resetadas! O progresso foi mantido.');
        atualizarPreview();
    }
}

function carregarConfiguracaoSalva() {
    const configuracaoSalva = localStorage.getItem('configuracaoRotinas');
    if (!configuracaoSalva) return;
    
    try {
        const configuracao = JSON.parse(configuracaoSalva);
        
        Object.keys(configuracao.dias).forEach(dia => {
            const diaConfig = configuracao.dias[dia];
            const nomeInput = document.querySelector(`.grupo[data-grupo="${dia}"] .nome-treino`);
            const recompensaInput = document.querySelector(`.grupo[data-grupo="${dia}"] .recompensa-dia`);
            const lista = document.querySelector(`.exercicios-lista[data-grupo="${dia}"]`);
            
            if (nomeInput) nomeInput.value = diaConfig.nome;
            if (recompensaInput) recompensaInput.value = diaConfig.recompensa;
            if (lista) {
                lista.innerHTML = '';
                diaConfig.habitos.forEach(habito => {
                    const habitoElem = document.createElement('div');
                    habitoElem.className = 'exercicio-selecionado';
                    habitoElem.innerHTML = `
                        <div>
                            <strong>${habito.nome}</strong>
                            ${habito.dica ? `<div class="exercicio-dica">💡 ${habito.dica}</div>` : ''}
                        </div>
                        <div class="exercicio-config">
                            <button class="btn-remover-exercicio">🗑️</button>
                        </div>
                    `;
                    lista.appendChild(habitoElem);
                });
                atualizarContador(dia);
            }
        });
        
        atualizarPreview();
        
    } catch (error) {
        console.error('Erro ao carregar configuração:', error);
    }
}

function atualizarPreview() {
    const container = document.getElementById('preview-container');
    const configuracaoSalva = localStorage.getItem('configuracaoRotinas');
    
    if (!configuracaoSalva) {
        container.innerHTML = `
            <div class="preview-placeholder">
                <i class="fas fa-calendar-check"></i>
                <p>Configure as rotinas para ver a pré-visualização.</p>
            </div>
        `;
        return;
    }
    
    try {
        const configuracao = JSON.parse(configuracaoSalva);
        let html = '<div class="preview-ciclo">';
        
        // Mostrar apenas os dias que existem na configuração
        const diasConfigurados = Object.keys(configuracao.dias);
        
        if (diasConfigurados.length === 0) {
            html = '<p>Nenhuma rotina configurada</p>';
        } else {
            html += '<h3>📅 Sua Semana de Hábitos</h3>';
            html += '<div class="ciclo-treinos">';
            
            diasConfigurados.forEach(dia => {
                const diaConfig = configuracao.dias[dia];
                html += `
                    <div class="treino-ciclo">
                        <h4>${diaConfig.nome}</h4>
                        ${diaConfig.recompensa ? `<div class="recompensa-info">🎁 Recompensa: ${diaConfig.recompensa}</div>` : ''}
                        <div class="exercicios-ciclo">
                            ${diaConfig.habitos.slice(0, 3).map(habito => 
                                `<span>${habito.nome}</span>`
                            ).join('')}
                            ${diaConfig.habitos.length > 3 ? `<span>+${diaConfig.habitos.length - 3} mais</span>` : ''}
                        </div>
                    </div>
                `;
            });
            
            html += '</div>';
        }
        
        html += '</div>';
        container.innerHTML = html;
        
    } catch (error) {
        container.innerHTML = '<p>Erro ao carregar pré-visualização.</p>';
    }
}

function mostrarFeedback(mensagem) {
    const feedbackAnterior = document.querySelector('.feedback-temporario');
    if (feedbackAnterior) {
        feedbackAnterior.remove();
    }
    
    const feedback = document.createElement('div');
    feedback.className = 'feedback-temporario';
    feedback.textContent = mensagem;
    document.body.appendChild(feedback);
    
    setTimeout(() => {
        feedback.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => feedback.remove(), 300);
    }, 3000);
}